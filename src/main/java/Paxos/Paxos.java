package Paxos;

import BlockChain.BlockChainServer;
import DataTypes.Block;
import Paxos.PaxosMsgs.*;
import Utils.JsonSerializer;
import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Paxos {
    private int qSize = 1; //TODO: testing
    static private BlockChainServer server;
    private int r = 0;
    private int lastGoodRound = 0;
    private int lastRound = 0;
    private boolean decided = false;
    private Block v;
    private List<Thread> acceptorsThreads = new ArrayList<>();
    private static Logger log = Logger.getLogger(LeaderFailureDetector.class.getName());
    Thread watcher = new Thread() {
        @Override
        public void run() {
            while(true) {
                try {
                    sleep(10); // TODO: ???
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                log.info("watcher thread");
                if (LeaderFailureDetector.leaderFailure) {
                    if (Integer.parseInt(LeaderFailureDetector.getCurrentLeader().split(":")[1]) == server.getId()) {
                        for (Thread t : acceptorsThreads) {
                            t.interrupt();
                        }
                    }
                    LeaderFailureDetector.leaderFailure = false;
                }
            }
        }
    };

    public void init(BlockChainServer s) throws IOException, InterruptedException, KeeperException {
        server = s;
        LeaderFailureDetector.connect();
        LeaderFailureDetector.setID(s.getAddress() + ":" + String.valueOf(s.getId()));
        LeaderFailureDetector.propose();
        LeaderFailureDetector.electLeader();
        watcher.start();
        log.info("watcher thread starting");
    }

    public Block propose(Block block) {
        v = block;
        while (!decided) {
            if (Integer.parseInt(LeaderFailureDetector.getCurrentLeader().split(":")[1]) == server.getId()) {
                log.info("starting proposer phase");
                proposerPhase();
            } else {
                acceptorsPhase(block);
            }
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
        for (Thread t : acceptorsThreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private Thread receiveMsgThread(Block b) {
        return new Thread() {
            @Override
            public void run() {
                for (String msg : server.receiveMessage(Peer.a_prepare).collect(Collectors.toList())) {
                    PrepareMsg m = (PrepareMsg) JsonSerializer.deserialize(msg, PrepareMsg.class);
                    if (m.serverID == getLeaderID()) {
                        if (m.blockNum <= server.getBCLength()) {
                            server.sendMessage(
                                    JsonSerializer.serialize(
                                            new PromiseMsg(server.getId(), m.r, PaxosMassegesTypes.NACK, lastGoodRound,
                                                    null, server.getBCLength(), server.getAddress())),
                                    m.serverAddr, Peer.l_promise);
                            break;
                        }
                        if (m.r > lastRound) {
                            server.sendMessage(
                                    JsonSerializer.serialize(
                                            new PromiseMsg(server.getId(), m.r, PaxosMassegesTypes.ACK, lastGoodRound,
                                                    b, server.getBCLength(), server.getAddress())),
                                    m.serverAddr, Peer.l_promise);
                            r = m.r;
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
        };
    }

    private Thread accMsgThread() {
        return new Thread() {
            @Override
            public void run() {
                for (String msg : server.receiveMessage(Peer.a_accept).collect(Collectors.toList())) {
                    AcceptMsg m = (AcceptMsg) JsonSerializer.deserialize(msg, AcceptMsg.class);
                    if (m.serverID == getLeaderID()) {
                        if (m.blockNum <= server.getBCLength()) {
                            server.sendMessage(
                                    JsonSerializer.serialize(
                                            new PromiseMsg(server.getId(), m.r, PaxosMassegesTypes.NACK, lastGoodRound,
                                                    null, server.getBCLength(), server.getAddress())),
                                    m.serverAddr, Peer.l_promise);
                            break;
                        }
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
        };
    }

    private Thread commitMsgThread() {
        return new Thread() {
            @Override
            public void run() {
                for (String msg : server.receiveMessage(Peer.a_commit).collect(Collectors.toList())) {
                    CommitMsg m = (CommitMsg) JsonSerializer.deserialize(msg, CommitMsg.class);
                    if (m.serverID == getLeaderID()) { // TODO: Can we remove it??
                        if (m.blockNum <= server.getBCLength()) {
                            server.sendMessage(
                                    JsonSerializer.serialize(
                                            new PromiseMsg(server.getId(), m.r, PaxosMassegesTypes.NACK, lastGoodRound,
                                                    null, server.getBCLength(), server.getAddress())),
                                    m.serverAddr, Peer.l_promise);
                            break;
                        }
                        decided = true;
                    }
                }
            }
        };
    }
    private int getLeaderID() {
        return Integer.parseInt(LeaderFailureDetector.getCurrentLeader().split(":")[1]);
    }

    private String getLeaderAddr() {
        return LeaderFailureDetector.getCurrentLeader().split(":")[0];
    }

    private void proposerPhase() {
        r = lastRound + 1;
        /* FIRST PHASE */
        server.broadcast(JsonSerializer.serialize(new PrepareMsg(server.getId(), r, server.getBCLength(), server.getAddress())), Peer.a_prepare);
        List<PromiseMsg> promSet = new ArrayList<>();
        promSet.add( new PromiseMsg(server.getId(), r, PaxosMassegesTypes.ACK, lastGoodRound,
                v, server.getBCLength(), server.getAddress()));
        while (promSet.size() < qSize) {
            for (String msg: server.receiveMessage(Peer.l_promise).collect(Collectors.toList())) {
                PromiseMsg pmsg = (PromiseMsg) JsonSerializer.deserialize(msg, PromiseMsg.class);
                if (pmsg.type.equals(PaxosMassegesTypes.PROMISE)) { //TODO: add support in rounds of paxos
                    promSet.add(pmsg);
                }
            }
        }
        promSet = promSet.stream().
                filter(promiseMsg -> promiseMsg.ack.equals(PaxosMassegesTypes.ACK)).
                collect(Collectors.toList());
        if (promSet.size() < qSize) {
            try {
                LeaderFailureDetector.restart();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        /* SECOND PHASE */
        v = selectVal(promSet);
        server.broadcast(JsonSerializer.serialize(new AcceptMsg(server.getId(), r, v, server.getBCLength(), server.getAddress())), Peer.a_accept);
        /* THIRD PHASE */
        List<AcceptedMsg> acceptedSet = new ArrayList<>();
        acceptedSet.add( new AcceptedMsg(server.getId(), r, PaxosMassegesTypes.ACK,
                server.getBCLength(), server.getAddress()));
        while (acceptedSet.size() < qSize) {
            for (String msg: server.receiveMessage(Peer.l_accepted).collect(Collectors.toList())) {
                AcceptedMsg amsg = (AcceptedMsg) JsonSerializer.deserialize(msg, AcceptedMsg.class);
                if (amsg.type.equals(PaxosMassegesTypes.ACCEPTED)) { //TODO: add support in rounds of paxos
                    acceptedSet.add(amsg);
                }
            }
        }
        acceptedSet = acceptedSet.stream().
                filter(acceptedMsg -> acceptedMsg.ack.equals(PaxosMassegesTypes.ACK)).
                collect(Collectors.toList());
        if (acceptedSet.size() < qSize) {
            try {
                LeaderFailureDetector.restart();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        server.broadcast(JsonSerializer.serialize(new CommitMsg(server.getId(), r, v, server.getBCLength(), server.getAddress())), Peer.a_commit); // TODO: r-cast???
        decided = true;
    }

    private Block selectVal(List<PromiseMsg> promSet) {
        promSet.sort((o1, o2) -> {
            if (o1.lastGoodRound > o2.lastGoodRound) return 1;
            if (o1.lastGoodRound < o2.lastGoodRound) return -1;
            if (o1.serverID > o2.serverID) return 1;
            return -1;
        });
        return promSet.get(0).block;
    }
}
