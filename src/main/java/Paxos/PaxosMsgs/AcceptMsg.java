package Paxos.PaxosMsgs;

import DataTypes.Block;

public class AcceptMsg extends PaxosMsg{
    public int r;
    public Block block;
    public int accepted_port;

    public AcceptMsg(int id, int _r, Block b, int b_num, String sAddr, int _accepted_port, int _round) {
        type = PaxosMassegesTypes.ACCEPT;
        serverAddr = sAddr;
        serverID = id;
        blockNum = b_num;
        r = _r;
        block = b;
        accepted_port =_accepted_port;
        round = _round;
//        port = _port;
    }
}
