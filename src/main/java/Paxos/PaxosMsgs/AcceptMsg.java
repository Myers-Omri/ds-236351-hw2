package Paxos.PaxosMsgs;

import DataTypes.Block;

public class AcceptMsg extends PaxosMsg{
    public int r;
    public Block block;

    public AcceptMsg(int id, int _r, Block b, int b_num, String sAddr) {
        type = PaxosMassegesTypes.ACCEPT;
        serverAddr = sAddr;
        serverID = id;
        blockNum = b_num;
        r = _r;
        block = b;
    }
}
