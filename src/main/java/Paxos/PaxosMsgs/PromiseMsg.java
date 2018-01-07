package Paxos.PaxosMsgs;

import DataTypes.Block;

public class PromiseMsg extends PaxosMsg {
    public int acc_r;
    public String ack;
    public int lastGoodRound;
    public Block block;

    public PromiseMsg(int id, int _r, String _ack, int lgr, Block b, int b_num, String sAddr, int _round) {
        type = PaxosMassegesTypes.PROMISE;
        serverAddr = sAddr;
        serverID = id;
        blockNum = b_num;
        acc_r = _r;
        ack = _ack;
        lastGoodRound = lgr;
        block = b;
        round = _round;
//        port = _port;
    }
}
