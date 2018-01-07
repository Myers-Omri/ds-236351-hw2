package Paxos;

import BlockChain.BlockChainServer;
import DataTypes.Block;
import Paxos.PaxosMsgs.*;
import Utils.Config;
import Utils.JsonSerializer;
import Utils.LeaderFailureDetector;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class Paxos {
    private int qSize; //TODO: testing
    private BlockChainServer server;
    private int r = 0;
    private int lastGoodRound = 0;
    private int lastRound = 0;
    private boolean decided = false;
    private Block v;
    private List<Thread> acceptorsThreads = new ArrayList<>();
    private Thread proposerThread;
    private static Logger log = Logger.getLogger(Paxos.class.getName());
    private int paxosRound = 0;
    private List<String> prepareMsgsQueue = new ArrayList<>();
    private List<String> acceptMsgsQueue = new ArrayList<>();
    private List<String> commitMsgsQueue = new ArrayList<>();
    private List<String> promiseMsgsQueue = new ArrayList<>();
    private List<String> acceptedMsgsQueue = new ArrayList<>();


    public Paxos(BlockChainServer s, int _q_size) {
        server = s;
        qSize = _q_size;
        log.info(format("Quorum size is [%d]", qSize));
    }
    private void init() {
//        server.clearSocket(PaxosMassegesTypes.ACCEPT);
//        server.clearSocket(PaxosMassegesTypes.ACCEPTED);
//        server.clearSocket(PaxosMassegesTypes.PREPARE);
//        server.clearSocket(PaxosMassegesTypes.PROMISE);
//        server.clearSocket(PaxosMassegesTypes.COMMIT);
        r = 0;
        lastGoodRound = 0;
        lastRound = 0;
        decided = false;
        paxosRound++;
    }
    public Block propose(Block block) {
        init();
        v = block;
        proposerThread = proposerPhaseThread();
        proposerThread.start();
        acceptorsPhase(block);
        try {
            for (Thread t : acceptorsThreads) {
                t.join();
            }
            acceptorsThreads = new ArrayList<>();
            proposerThread.join();
            proposerThread = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        decided = false;
        return v;
    }

    private void acceptorsPhase(Block b) {
        acceptorsThreads.add(receiveMsgThread(b));
        acceptorsThreads.add(accMsgThread());
        acceptorsThreads.add(commitMsgThread());

        for (Thread t : acceptorsThreads) {
            t.start();
        }
    }

    private Thread receiveMsgThread(Block b) {
        return new Thread() {
            @Override
            public void run() {
                log.info("starting receive phase");
                while (!decided) {
                    prepareMsgsQueue.addAll(server.receiveMessage(PaxosMassegesTypes.PREPARE));
                    List<String> removed = new ArrayList<>();
                    for (String msg : prepareMsgsQueue) {
                        PrepareMsg m = (PrepareMsg) JsonSerializer.deserialize(msg, PrepareMsg.class);
                        if (m.round > paxosRound) {
                            continue;
                        }
                        if (m.serverID == getLeaderID()) {
                            removed.add(msg);
                            if (m.round < paxosRound) {
                                log.info(format("corrupted PREPARE message from [%s]:[%s]", m.serverAddr, JsonSerializer.serialize(v)));
                                continue;
                            }
                            log.info(format("got a PREPARE message with r=%d, lastRound=%d from server [%s]", m.r, lastRound, m.serverID));
                            if (m.r > lastRound) {
                                server.sendMessage(
                                        JsonSerializer.serialize(
                                                new PromiseMsg(server.getId(), m.r, PaxosMassegesTypes.ACK, lastGoodRound,
                                                        b, server.getBCLength(), Config.addr, paxosRound)),
                                        m.serverAddr, m.promise_port);
                                lastRound = m.r;
                                break;
                            } else {
                                server.sendMessage(
                                        JsonSerializer.serialize(
                                                new PromiseMsg(server.getId(), m.r, PaxosMassegesTypes.NACK, lastGoodRound,
                                                        null, server.getBCLength(), server.getAddress(), paxosRound)),
                                        m.serverAddr, m.promise_port);
//                                break;
                            }

                        }
                    }
                    prepareMsgsQueue.removeAll(removed);
                }
            }
        };
    }

    private Thread accMsgThread() {
        return new Thread() {
            @Override
            public void run() {
                log.info("starting accept phase");
                while(!decided) {
                    acceptMsgsQueue.addAll(server.receiveMessage(PaxosMassegesTypes.ACCEPT));
                    List<String> removed = new ArrayList<>();
                    for (String msg : acceptMsgsQueue) {
                        AcceptMsg m = (AcceptMsg) JsonSerializer.deserialize(msg, AcceptMsg.class);
                        if (m.round > paxosRound) {
                            continue;
                        }
                        if (m.serverID == getLeaderID()) {
                            log.info(format("got an ACCEPT message with r=%d, lastRound=%d from server [%s]",
                                    m.r, lastRound, m.serverID));
                            removed.add(msg);
                            if (m.round < paxosRound) {
                                log.info(format("corrupted ACCEPT message from [%s]:[%s]", m.serverAddr, JsonSerializer.serialize(v)));
                                continue;
                            }

                            if (m.r == lastRound) {
                                lastGoodRound = m.r;
                                v = m.block;
                                server.sendMessage(
                                        JsonSerializer.serialize(
                                                new AcceptedMsg(server.getId(), m.r, PaxosMassegesTypes.ACK,
                                                        server.getBCLength(), server.getAddress(), paxosRound)),
                                        m.serverAddr, m.accepted_port);
                            } else {
                                server.sendMessage(
                                        JsonSerializer.serialize(
                                                new AcceptedMsg(server.getId(), m.r, PaxosMassegesTypes.NACK,
                                                        server.getBCLength(), server.getAddress(), paxosRound)),
                                        m.serverAddr, m.accepted_port);
                            }
                        }
                    }
                    acceptMsgsQueue.removeAll(removed);
                }
            }
        };
    }

    private Thread commitMsgThread() {
        return new Thread() {
            @Override
            public void run() {
                log.info("starting commit phase");
                while (!decided) {
                    commitMsgsQueue.addAll(server.receiveMessage(PaxosMassegesTypes.COMMIT));
//                    String cmsg = server.receiveSingleMsg(PaxosMassegesTypes.COMMIT);
                    List<String> removed = new ArrayList<>();
                    for (String msg : commitMsgsQueue) {
                        CommitMsg m = (CommitMsg) JsonSerializer.deserialize(msg, CommitMsg.class);
                        removed.add(msg);
                        if (m.round < paxosRound) {
                            log.info(format("corrupted COMMIT message from [%s]:[%s]", m.serverAddr, JsonSerializer.serialize(v)));
                            continue;
                        }
                        log.info(format("got a COMMIT message from [%s]:[%s]", m.serverAddr, JsonSerializer.serialize(v)));
                        v = m.block;
                        decided = true;
                        server.broadcast(JsonSerializer.serialize(new CommitMsg(Config.id, lastGoodRound, v,
                                        server.getBCLength(), Config.addr, paxosRound)),
                                PaxosMassegesTypes.COMMIT); // TODO: remove that on real bc
                        acceptorsThreads.get(0).interrupt();
                        acceptorsThreads.get(1).interrupt();
                        return;
                    }
                    commitMsgsQueue.removeAll(removed);
                }
            }
        };
    }
    static private int getLeaderID() {
        return Integer.parseInt(LeaderFailureDetector.getCurrentLeader().split(":")[1]);
    }

//    static private String getLeaderAddr() {
//        return LeaderFailureDetector.getCurrentLeader().split(":")[0];
//    }

    private Thread proposerPhaseThread() {
        return new Thread() {
            @Override
            public void run() {
                proposerPhase();
            }
        };
    }
    private void proposerPhase() {
        while (!decided) {
            if (!(Integer.parseInt(LeaderFailureDetector.getCurrentLeader().split(":")[1]) == server.getId())) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                log.info(format("[%d] starting proposer phase", server.getId()));
                r = lastRound + 1;
                /* FIRST PHASE */
                server.broadcast(JsonSerializer.serialize(new PrepareMsg(server.getId(), r, server.getBCLength(),
                        Config.addr, Config.l_promise, paxosRound)), PaxosMassegesTypes.PREPARE);
                log.info(format("[%d] leader has broadcast PREPARE MSG", server.getId()));
                List<PromiseMsg> promSet = new ArrayList<>();
//        promSet.add( new PromiseMsg(server.getId(), r, PaxosMassegesTypes.ACK, lastGoodRound,
//                v, server.getBCLength(), server.getAddress()));
                while (promSet.size() < qSize) {
                    promiseMsgsQueue.addAll(server.receiveMessage(PaxosMassegesTypes.PROMISE));
                    List<String> removed = new ArrayList<>();
                    for (String msg: promiseMsgsQueue) {
                        PromiseMsg pmsg = (PromiseMsg) JsonSerializer.deserialize(msg, PromiseMsg.class);
                        if (pmsg.round > paxosRound) continue; //TODO: should be change??
                        removed.add(msg);
                        if (pmsg.round < paxosRound) {
                            log.info(format("corrupted PROMISE message from [%s]:[%s]", pmsg.serverAddr, JsonSerializer.serialize(v)));
                            continue;
                        }
                        if (pmsg.type.equals(PaxosMassegesTypes.PROMISE)) { //TODO: add support in rounds of paxos
                            log.info(format("[%d] leader has accepted PROMISE message", server.getId()));
                            promSet.add(pmsg);
                        }
                    }
                    promiseMsgsQueue.removeAll(removed);
                }
                promSet = promSet.stream().
                        filter(promiseMsg -> promiseMsg.ack.equals(PaxosMassegesTypes.ACK)).
                        collect(Collectors.toList());
                if (promSet.size() < qSize) continue;
                /* SECOND PHASE */
                v = selectVal(promSet);
                server.broadcast(JsonSerializer.serialize(new AcceptMsg(server.getId(), r, v, server.getBCLength(),
                        server.getAddress(), Config.l_accepted, paxosRound)), PaxosMassegesTypes.ACCEPT);
                /* THIRD PHASE */
                List<AcceptedMsg> acceptedSet = new ArrayList<>();
//        acceptedSet.add( new AcceptedMsg(server.getId(), r, PaxosMassegesTypes.ACK,
//                server.getBCLength(), server.getAddress()));
                while (acceptedSet.size() < qSize) {
                    acceptedMsgsQueue.addAll(server.receiveMessage(PaxosMassegesTypes.ACCEPTED));
                    List<String> removed = new ArrayList<>();
                    for (String msg: acceptedMsgsQueue) {
                        AcceptedMsg amsg = (AcceptedMsg) JsonSerializer.deserialize(msg, AcceptedMsg.class);
                        if (amsg.round > paxosRound) continue;
                        removed.add(msg);
                        if (amsg.round < paxosRound) {
                            log.info(format("corrupted ACCEPTED message from [%s]:[%s]", amsg.serverAddr, JsonSerializer.serialize(v)));
                            continue;
                        }
                        if (amsg.type.equals(PaxosMassegesTypes.ACCEPTED)) { //TODO: add support in rounds of paxos
                            log.info(format("[%d] leader has accepted ACCEPTED message", server.getId()));
                            acceptedSet.add(amsg);
                        }
                    }
                    acceptedMsgsQueue.removeAll(removed);
                }
                acceptedSet = acceptedSet.stream().
                        filter(acceptedMsg -> acceptedMsg.ack.equals(PaxosMassegesTypes.ACK)).
                        collect(Collectors.toList());
                if (acceptedSet.size() < qSize) continue;
                server.broadcast(JsonSerializer.serialize(new CommitMsg(server.getId(), r, v, server.getBCLength(),
                        server.getAddress(), paxosRound)), PaxosMassegesTypes.COMMIT); // TODO: r-cast???
                log.info(format("[%d] leader has broadcast COMMIT message [%s]", Config.id, JsonSerializer.serialize(v)));
                decided = true;
                return;
            }
        }
    }

    private boolean ValidateQuorum(int size) {
        log.info(format("[%d] leader has accepted %d messages", server.getId(), size));
        if (size < qSize) {
            try {
                LeaderFailureDetector.yelidLeaderShip(server.getAddress(), server.getId()); // Gives up on leadership
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    static private Block selectVal(List<PromiseMsg> promSet) {
        promSet.sort((o1, o2) -> {
            if (o1.lastGoodRound > o2.lastGoodRound) return 1;
            if (o1.lastGoodRound < o2.lastGoodRound) return -1;
            if (o1.serverID > o2.serverID) return 1;
            return -1;
        });
        return promSet.get(0).block;
    }

    public void stopPaxos() {
        for (Thread t : acceptorsThreads) {
            t.interrupt();
        }
    }

    public void setQuorum(int q) {
        qSize = q;
    }

    public int getQuorum() {
        return qSize;
    }
}

