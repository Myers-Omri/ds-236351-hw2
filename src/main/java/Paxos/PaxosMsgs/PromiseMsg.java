package Paxos.PaxosMsgs;

import DataTypes.Block;

import java.util.List;

public class PromiseMsg extends PaxosMsg {
    public int acc_r;
    public String ack;
    public int lastGoodRound;
    public List<Block> block;

    public PromiseMsg(int id, int _r, String _ack, int lgr, List<Block> b, String sAddr, int _round) {
        type = PaxosMassegesTypes.PROMISE;
        serverAddr = sAddr;
        serverID = id;
        acc_r = _r;
        ack = _ack;
        lastGoodRound = lgr;
        block = b;
        round = _round;
    }
}
