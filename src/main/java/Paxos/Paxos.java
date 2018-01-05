package Paxos;

import BlockChain.BlockChainServer;
import DataTypes.Block;
import Paxos.PaxosMsgs.*;
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


    public Paxos(BlockChainServer s, int _q_size) {
        server = s;
        qSize = _q_size;
    }

    public Block propose(Block block) {
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
                    for (String msg : server.receiveMessage(Peer.a_prepare)) {
                        PrepareMsg m = (PrepareMsg) JsonSerializer.deserialize(msg, PrepareMsg.class);
                        if (m.serverID == getLeaderID()) {
//                            if (m.blockNum <= server.getBCLength()) {
//                                server.sendMessage(
//                                        JsonSerializer.serialize(
//                                                new PromiseMsg(server.getId(), m.r, PaxosMassegesTypes.NACK, lastGoodRound,
//                                                        null, server.getBCLength(), server.getAddress())),
//                                        m.serverAddr, Peer.l_promise);
//                                break;
//                            }
                            log.debug(format("got a PREPARE message with r=%d, lastRound=%d", m.r, lastRound));
                            if (m.r > lastRound) {
                                server.sendMessage(
                                        JsonSerializer.serialize(
                                                new PromiseMsg(server.getId(), m.r, PaxosMassegesTypes.ACK, lastGoodRound,
                                                        b, server.getBCLength(), server.getAddress())),
                                        m.serverAddr, Peer.l_promise);
                                lastRound = m.r;
                                break;
                            } else {
                                server.sendMessage(
                                        JsonSerializer.serialize(
                                                new PromiseMsg(server.getId(), m.r, PaxosMassegesTypes.NACK, lastGoodRound,
                                                        null, server.getBCLength(), server.getAddress())),
                                        m.serverAddr, Peer.l_promise);
                                break;
                            }
                        }
                    }
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
                    for (String msg : server.receiveMessage(Peer.a_accept)) {
                        AcceptMsg m = (AcceptMsg) JsonSerializer.deserialize(msg, AcceptMsg.class);
                        if (m.serverID == getLeaderID()) {
                            log.debug(format("got an ACCEPT message with r=%d, lastRound=%d", m.r, lastRound));
//                            if (m.blockNum <= server.getBCLength()) {
//                                server.sendMessage(
//                                        JsonSerializer.serialize(
//                                                new PromiseMsg(server.getId(), m.r, PaxosMassegesTypes.NACK, lastGoodRound,
//                                                        null, server.getBCLength(), server.getAddress())),
//                                        m.serverAddr, Peer.l_promise);
//                                break;
//                            }
                            if (m.r == lastRound) {
                                lastGoodRound = m.r;
                                v = m.block;
                                server.sendMessage(
                                        JsonSerializer.serialize(
                                                new AcceptedMsg(server.getId(), m.r, PaxosMassegesTypes.ACK,
                                                        server.getBCLength(), server.getAddress())),
                                        getLeaderAddr(), Peer.l_accepted);
                            } else {
                                server.sendMessage(
                                        JsonSerializer.serialize(
                                                new AcceptedMsg(server.getId(), m.r, PaxosMassegesTypes.NACK,
                                                        server.getBCLength(), server.getAddress())),
                                        getLeaderAddr(), Peer.l_accepted);
                            }
                        }
                    }
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
                    for (String msg : server.receiveMessage(Peer.a_commit)) {
                        CommitMsg m = (CommitMsg) JsonSerializer.deserialize(msg, CommitMsg.class);
//                        if (m.blockNum <= server.getBCLength()) {
//
//                        }
                        log.debug(format("got an COMMIT message"));
//                        server.sendMessage(
//                                JsonSerializer.serialize(
//                                        new PromiseMsg(server.getId(), m.r, PaxosMassegesTypes.NACK, lastGoodRound,
//                                                null, server.getBCLength(), server.getAddress())),
//                                m.serverAddr, Peer.l_promise);

//                        if (m.serverID == getLeaderID()) { // TODO: Can we remove it??
//
//                        }

                        decided = true;
                        server.broadcast(JsonSerializer.serialize(new CommitMsg(server.getId(), lastGoodRound, v,
                                server.getBCLength(), server.getAddress())),
                                Peer.a_commit); // TODO: remove that on real bc
                        acceptorsThreads.get(0).interrupt();
                        acceptorsThreads.get(1).interrupt();
                        return;
                    }
                }
            }
        };
    }
    static private int getLeaderID() {
        return Integer.parseInt(LeaderFailureDetector.getCurrentLeader().split(":")[1]);
    }

    static private String getLeaderAddr() {
        return LeaderFailureDetector.getCurrentLeader().split(":")[0];
    }

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
                log.debug(format("[%d] starting proposer phase", server.getId()));
                r = lastRound + 1;
                /* FIRST PHASE */
                server.broadcast(JsonSerializer.serialize(new PrepareMsg(server.getId(), r, server.getBCLength(), server.getAddress())), Peer.a_prepare);
                log.debug(format("[%d] leader has broadcast PREPARE MSG", server.getId()));
                List<PromiseMsg> promSet = new ArrayList<>();
//        promSet.add( new PromiseMsg(server.getId(), r, PaxosMassegesTypes.ACK, lastGoodRound,
//                v, server.getBCLength(), server.getAddress()));
                while (promSet.size() < qSize) {
                    for (String msg: server.receiveMessage(Peer.l_promise)) {
                        PromiseMsg pmsg = (PromiseMsg) JsonSerializer.deserialize(msg, PromiseMsg.class);
                        if (pmsg.type.equals(PaxosMassegesTypes.PROMISE)) { //TODO: add support in rounds of paxos
                            log.debug(format("[%d] leader has accepted PROMISE message", server.getId()));
                            promSet.add(pmsg);
                        }
                    }
                }
                promSet = promSet.stream().
                        filter(promiseMsg -> promiseMsg.ack.equals(PaxosMassegesTypes.ACK)).
                        collect(Collectors.toList());
                if (ValidateQuorum(promSet.size())) return;
                /* SECOND PHASE */
                v = selectVal(promSet);
                server.broadcast(JsonSerializer.serialize(new AcceptMsg(server.getId(), r, v, server.getBCLength(), server.getAddress())), Peer.a_accept);
                /* THIRD PHASE */
                List<AcceptedMsg> acceptedSet = new ArrayList<>();
//        acceptedSet.add( new AcceptedMsg(server.getId(), r, PaxosMassegesTypes.ACK,
//                server.getBCLength(), server.getAddress()));
                while (acceptedSet.size() < qSize) {
                    for (String msg: server.receiveMessage(Peer.l_accepted)) {
                        AcceptedMsg amsg = (AcceptedMsg) JsonSerializer.deserialize(msg, AcceptedMsg.class);
                        if (amsg.type.equals(PaxosMassegesTypes.ACCEPTED)) { //TODO: add support in rounds of paxos
                            log.debug(format("[%d] leader has accepted ACCEPTED message", server.getId()));
                            acceptedSet.add(amsg);
                        }
                    }
                }
                acceptedSet = acceptedSet.stream().
                        filter(acceptedMsg -> acceptedMsg.ack.equals(PaxosMassegesTypes.ACK)).
                        collect(Collectors.toList());
                if (ValidateQuorum(acceptedSet.size())) return;
                server.broadcast(JsonSerializer.serialize(new CommitMsg(server.getId(), r, v, server.getBCLength(), server.getAddress())), Peer.a_commit); // TODO: r-cast???
                log.debug(format("[%d] leader has broadcast COMMIT message", server.getId()));
                decided = true;
                return;
            }
        }
    }

    private boolean ValidateQuorum(int size) {
        log.debug(format("[%d] leader has accepted %d messages", server.getId(), size));
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

