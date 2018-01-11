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
import static java.lang.StrictMath.max;
import static java.lang.String.format;
import static java.util.concurrent.Executors.newFixedThreadPool;

public class Paxos {
    private int qSize;
    private BlockChainServer server;
    public int lastGoodRound = 0;
    public int lastRound = 0;
    public int r = 0;
    public int paxosNum;
    public boolean decided = false;
    private List<Block> v = null;
    private ExecutorService tp = newFixedThreadPool(10);
    private static Logger log = Logger.getLogger(Paxos.class.getName());

    public Paxos(BlockChainServer s, List<Block> v, int lastGoodRound, int lastRound, int _q_size, int _paxosNum) {
        this.server = s;
        this.qSize = _q_size;
        this.paxosNum = _paxosNum;
        this.lastGoodRound = lastGoodRound;
        this.lastRound = lastRound;
        this.v = v;
        log.info(format("Quorum size is [%d]", qSize));
        log.info(format("PaxosNum is [%d]", paxosNum));
    }
    private void init(Block b) {
        decided = false;
        if (v == null) {
            v = new ArrayList<Block>();
            v.add(b);
        }
    }
    private void acceptorPreparePreviousRoundPhase(PrepareMsg msg) {
        log.info(format("[%d] starts acceptorPreparePreviuseRoundPhase phase round [%d]", Config.id, paxosNum));
        String ack = NACK;
        List<Block> v_ = null;
        PaxosDecision d = server.decided.get(msg.round);
        if (msg.r > d.lastRound) {
            ack = ACK;
            v_ = d.v;
            d.lastRound = msg.r;
        }
        String strMsg = serialize(new PromiseMsg(Config.id, msg.r,
                ack, d.lastGoodRound, v_, Config.addr, d.paxosNum));
        server.msn.sendMassageToLeader(strMsg, msg.serverID);
    }
    private void acceptorPreparePhase(PrepareMsg msg) {
        if (msg.round < paxosNum) {
            acceptorPreparePreviousRoundPhase(msg);
            return;
        }
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

    private void acceptorAcceptPreviousRoundPhase(AcceptMsg msg) {
        log.info(format("[%d] starts acceptorAcceptPreviousRoundPhase round [%d]", Config.id, paxosNum));
        PaxosDecision d = server.decided.get(msg.round);
        String ack = NACK;
        if (msg.r == d.lastRound) {
            d.lastGoodRound = lastRound;
            log.info(format("[%d] WARNING does those blocks changed??? [%s---->%s]",
                    Config.id, serialize(d.v), serialize(msg.blocks)));
            d.v = msg.blocks; // TODO: shouldn't be changed, so we will disable it
            ack = ACK;
        }
        String strMsg = serialize(new AcceptedMsg(Config.id, msg.r, ack, Config.addr, d.paxosNum));
        server.msn.sendMassageToLeader(strMsg, msg.serverID);
    }
    private void acceptorAcceptPhase(AcceptMsg msg) {
        if (msg.round < paxosNum) {
            acceptorAcceptPreviousRoundPhase(msg);
            return;
        }
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
    // TODO: no need for previous commit phase!!!
    private void acceptorCommitPhase(CommitMsg msg) {
        if (!decided) {
            v = msg.blocks;
//            String strMsg = serialize(new CommitMsg(Config.id, v, Config.addr, paxosNum));
//            server.msn.broadcastToAcceptors(strMsg, msg.serverID);
//            server.currentServerId = msg.serverID;
            log.info(format("[%d] decided round [%d]", Config.id, paxosNum));
            decided = true;
        }
    }

    private void leaderPreparePhase() {
        log.info(format("[%d] starts leaderPreparePhase phase round [%d]", Config.id, paxosNum));
        r = lastRound + 1;
        String strMsg = serialize(new PrepareMsg(Config.id, r, Config.addr, paxosNum));
        server.msn.broadcastToAcceptors(strMsg, -1);
    }

    private boolean leaderPromisePhase(List<PromiseMsg> pMsgs) {
        log.info(format("[%d] starts leaderPromisePhase phase round [%d]", Config.id, paxosNum));
        List<PromiseMsg> vpMsgs = pMsgs.stream().
                filter(msg-> msg.acc_r == r && msg.ack.equals(ACK)).
                collect(Collectors.toList());
        if (vpMsgs.size() < qSize) {
            log.info(format("[%d] finished leaderPromisePhase phase round [%d] ERROR", Config.id, paxosNum));
            return false;
        }
        v = selectVal(pMsgs);
        String strMsg = serialize(new AcceptMsg(Config.id, r, v, Config.addr, paxosNum));
        server.msn.broadcastToAcceptors(strMsg, -1);
        log.info(format("[%d] finished leaderPromisePhase phase round [%d] SUCCESS", Config.id, paxosNum));
        return true;
    }

    private boolean leaderAcceptedPhase(List<AcceptedMsg> aMsgs) {
        log.info(format("[%d] starts leaderAcceptedPhase phase round [%d]", Config.id, paxosNum));
        List<AcceptedMsg> vaMsgs = aMsgs.stream().
                filter(msg-> msg.r == r && msg.ack.equals(ACK)).
                collect(Collectors.toList());
        if (vaMsgs.size() < qSize) {
            return false;
        }
        String strMsg = serialize(new CommitMsg(Config.id, v, Config.addr, paxosNum));
        server.msn.broadcastToAcceptors(strMsg, -1); //TODO: BUG alerts!!
//        decided = true;
        return true;
    }

    private void  runPropose() {
            tp.execute(this::runLeaderPhase);
            acceptorPhase();
    }
    private boolean isLeaderChanged(int leaderID) {
       if (leaderID == LeaderFailureDetector.getCurrentLeaderId()
               && leaderID != server.currentServerId) {
           log.info(format("[%d] known leader has changed [%d]", Config.id, leaderID));
           server.currentServerId = leaderID;
           return true;
       }
       return false;
    }
    private void acceptorPhase() {
        while (!decided) {
            Object msg = server.msn.receiveAcceptorMsg(paxosNum);
            if (!isLeaderChanged(((PaxosMsg)msg).serverID)) {
                if (((PaxosMsg) msg).round < paxosNum) {
                    log.info(format("[%d] skipped massage from [%d]", Config.id, ((PaxosMsg) msg).serverID));
                    continue;
                }
            }
//            if (leaderPhase < paxosNum) {
//                decided = true;
//                return leaderPhase;
//            }
            if (msg instanceof PrepareMsg) {
                log.info(format("[%d] accepted prepare Msg on round [%d] from [%d]", Config.id, paxosNum, ((PrepareMsg) msg).serverID));
                acceptorPreparePhase((PrepareMsg) msg);
            } else if (msg instanceof AcceptMsg) {
                log.info(format("[%d] accepted prepare Msg on round [%d] from [%d]", Config.id, paxosNum, ((AcceptMsg) msg).serverID));
                acceptorAcceptPhase((AcceptMsg) msg);
            } else if (msg instanceof CommitMsg) {
                log.info(format("[%d] accepted prepare Msg on round [%d] from [%d]", Config.id, paxosNum, ((CommitMsg) msg).serverID));
                acceptorCommitPhase((CommitMsg) msg);
            }
        }
    }
    private void runLeaderPhase() {
        while (!decided) {
            while (!(Integer.parseInt(getCurrentLeader().split(":")[1]) == Config.id)) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    log.info(format("[Exception [%d]] ", paxosNum), e);
                    return;
                }
            }
            if (!server.isLeader) {
                retransmitPrevBlocks();
            }
            log.info(format("[%d] starts leader phase round [%d]", Config.id, paxosNum));
            server.isLeader = true;
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
            log.info(format("[%d] leader have a quorum, of PROMISE massages round [%d]", Config.id, paxosNum));
            if (!leaderPromisePhase(pMsgs)) continue;
            while (aMsgs.size() < qSize) {
                Object msg = server.msn.receiveLeaderMsg(paxosNum);
                if (msg instanceof AcceptedMsg) {
                    if (((AcceptedMsg) msg).r == r) {
                        aMsgs.add((AcceptedMsg) msg);
                    }
                }
            }
            log.info(format("[%d] leader have a quorum, of ACCEPTED massages round [%d]", Config.id, paxosNum));
            if(leaderAcceptedPhase(aMsgs)) return;
        }
    }

    public PaxosDecision propose(Block b) {
        init(b);
        runPropose();
        return new PaxosDecision(v, lastGoodRound, lastRound, paxosNum);
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
    public void retransmitPrevBlocks() {
        int blockNum = server.decided.size();
        int startIndex = max(0, (qSize - 1));
        for (int i = startIndex ; i < blockNum ; i++) {
            log.info(format("[%d] retransmit decision [%d]", Config.id, i));
            PaxosDecision d = server.decided.get(i);
            String msgStr = serialize(new CommitMsg(Config.id, d.v, Config.addr, d.paxosNum));
            server.msn.broadcastToAcceptors(msgStr, Config.id);
        }
    }
    public void setQuorum(int q) {
        qSize = q;
    }

    public int getQuorum() {
        return qSize;
    }
}

