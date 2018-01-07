package Paxos.PaxosMsgs;

import DataTypes.Block;

import java.util.ArrayList;
import java.util.List;

public class AcceptMsg extends PaxosMsg{
    public int r;
    public List<Block> blocks;
    public int accepted_port;

    public AcceptMsg(int id, int _r, List<Block> b, int b_num, String sAddr, int _accepted_port, int _round) {
        type = PaxosMassegesTypes.ACCEPT;
        serverAddr = sAddr;
        serverID = id;
        blockNum = b_num;
        r = _r;
        blocks = b;
        accepted_port =_accepted_port;
        round = _round;
//        port = _port;
    }
}
