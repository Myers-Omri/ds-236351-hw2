package Paxos;

import BlockChain.BlockChainServer;
import DataTypes.Block;
import Paxos.PaxosMsgs.*;
import Utils.Config;
import Utils.JsonSerializer;
import Utils.LeaderFailureDetector;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static Paxos.PaxosMsgs.PaxosMassegesTypes.*;
import static Utils.JsonSerializer.deserialize;
import static Utils.JsonSerializer.serialize;
import static Utils.LeaderFailureDetector.getCurrentLeader;
import static java.lang.String.format;
import static java.util.concurrent.Executors.newFixedThreadPool;

public class Paxos {
    private int qSize;
    private BlockChainServer server;
    private int lastGoodRound = 0;
    private int lastRound = 0;
    private int r = 0;
    private int paxosNum;
    private boolean decided = false;
    private boolean leader = false;
    private List<Block> v;
    private ExecutorService tp = newFixedThreadPool(6);
    private static Logger log = Logger.getLogger(Paxos.class.getName());

    public Paxos(BlockChainServer s, int _q_size, int _paxosNum) {
        server = s;
        qSize = _q_size;
        paxosNum = _paxosNum;
        log.info(format("Quorum size is [%d]", qSize));
    }
    private void init(Block b) {
        lastGoodRound = 0;
        lastRound = 0;
        r = 0;
        decided = false;
        v = new ArrayList<Block>();
        v.add(b);
    }

    private void acceptorPreparePhase(PrepareMsg msg) {
        log.info(format("[%d] starts acceptorPreparePhase phase round [%d]", Config.id, paxosNum));
        String ack = NACK;
        List<Block> v_ = null;
        if (msg.r > lastRound) {
            ack = ACK;
            v_ = v;
            lastRound = msg.r;
        }
        String strMsg = serialize(new PromiseMsg(Config.id, msg.r,
                ack, lastGoodRound, v_, Config.addr, paxosNum));
        server.msn.sendMassageToLeader(strMsg, msg.serverID);
    }

    private void acceptorAcceptPhase(AcceptMsg msg) {
        log.info(format("[%d] starts acceptorAcceptPhase phase round [%d]", Config.id, paxosNum));
        String ack = NACK;
        if (msg.r == lastRound) {
            lastGoodRound = lastRound;
            v = msg.blocks;
            ack = ACK;
        }
        String strMsg = serialize(new AcceptedMsg(Config.id, msg.r, ack, Config.addr, paxosNum));
        server.msn.sendMassageToLeader(strMsg, msg.serverID);
    }

    private void acceptorCommitPhase(CommitMsg msg) {
        log.info(format("[%d] starts acceptorCommitPhase phase round [%d]", Config.id, paxosNum));
        if (!decided) {
            decided = true;
            String strMsg = serialize(msg);
            server.msn.broadcast(strMsg);
        }
    }

    private void leaderPreparePhase() {
        log.info(format("[%d] starts leaderPreparePhase phase round [%d]", Config.id, paxosNum));
        r = lastRound + 1;
        String strMsg = serialize(new PrepareMsg(Config.id, r, Config.addr, paxosNum));
        server.msn.broadcast(strMsg);
    }

    private void leaderPromisePhase(List<PromiseMsg> pMsgs) {
        log.info(format("[%d] starts leaderPromisePhase phase round [%d]", Config.id, paxosNum));
        List<PromiseMsg> vpMsgs = pMsgs.stream().
                filter(msg-> msg.acc_r == r && msg.ack.equals(ACK)).
                collect(Collectors.toList());
        if (vpMsgs.size() < qSize) {
            log.info(format("[%d] finished leaderPromisePhase phase round [%d] ERROR", Config.id, paxosNum));
            return;
        }
        v = selectVal(pMsgs);
        String strMsg = serialize(new AcceptMsg(Config.id, r, v, Config.addr, paxosNum));
        server.msn.broadcast(strMsg);
        log.info(format("[%d] finished leaderPromisePhase phase round [%d] SUCCESS", Config.id, paxosNum));
    }

    private void leaderAcceptedPhase(List<AcceptedMsg> aMsgs) {
        log.info(format("[%d] starts leaderAcceptedPhase phase round [%d]", Config.id, paxosNum));
        List<AcceptedMsg> vaMsgs = aMsgs.stream().
                filter(msg-> msg.r == r && msg.ack.equals(ACK)).
                collect(Collectors.toList());
        if (vaMsgs.size() < qSize) {
            return;
        }
        String strMsg = serialize(new CommitMsg(Config.id, r, v, Config.addr, paxosNum));
        server.msn.broadcast(strMsg);
        decided = true;
    }

    private void runPropose() {
        while (!decided) {
            if ((Integer.parseInt(getCurrentLeader().split(":")[1]) == Config.id) && !leader) {
                leader = true;
                tp.execute(this::runLeaderPhase);
            }
            log.info(format("[%d] starts acceptor phase round [%d]", Config.id, paxosNum));
            Object msg = server.msn.receiveAcceptorMsg(paxosNum);
            if (msg instanceof PrepareMsg) {
                tp.execute(()->acceptorPreparePhase((PrepareMsg) msg));
            } else if (msg instanceof AcceptMsg) {
                tp.execute(()->acceptorAcceptPhase((AcceptMsg) msg));
            } else if (msg instanceof CommitMsg) {
                tp.execute(()->acceptorCommitPhase((CommitMsg) msg));
            }
        }
    }

    private void runLeaderPhase() {
        log.info(format("[%d] starts leader phase round [%d]", Config.id, paxosNum));
        leaderPreparePhase();
        List<PromiseMsg> pMsgs = new ArrayList<>();
        List<AcceptedMsg> aMsgs = new ArrayList<>();
        while (pMsgs.size() < qSize) {
            Object msg = server.msn.receiveLeaderMsg(paxosNum);
            if (msg instanceof PromiseMsg) {
                if (((PromiseMsg) msg).acc_r == r) {
                    pMsgs.add((PromiseMsg) msg);
                }
            }
        }
        leaderPromisePhase(pMsgs);
        while (aMsgs.size() < qSize) {
            Object msg = server.msn.receiveLeaderMsg(paxosNum);
            if (msg instanceof AcceptedMsg) {
                if (((AcceptedMsg) msg).r == r) {
                    aMsgs.add((AcceptedMsg) msg);
                }
            }
        }
        leaderAcceptedPhase(aMsgs);
        leader = false;
    }

    public List<Block> propose(Block b) {
        init(b);
        runPropose(); //TODO: cleanup???
        return v;
    }

    private List<Block> selectVal(List<PromiseMsg> promSet) {
        int maxLGR = promSet.stream().max(Comparator.comparingInt(m1 -> m1.lastGoodRound)).get().lastGoodRound;
        promSet = promSet.stream().filter(m -> m.lastGoodRound == maxLGR).collect(Collectors.toList());
        List<Block> res = new ArrayList<>();
        for (PromiseMsg p : promSet) {
            for (Block b : p.block) {
                if (server.validateBlock(b, res)) {
                    res.add(b);
                }
            }
        }
        long fHash = server.getBlockchain().get(server.getBCLength() - 1).prevBlockHash + 1;
        for (Block b : res) {
            b.prevBlockHash = fHash;
            fHash++;
        }
        return res;
    }

    public void stopPaxos() {
        tp.shutdownNow();
    }

    public void setQuorum(int q) {
        qSize = q;
    }

    public int getQuorum() {
        return qSize;
    }
}

