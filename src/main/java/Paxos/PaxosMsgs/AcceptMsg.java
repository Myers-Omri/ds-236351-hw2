package Paxos.PaxosMsgs;

import DataTypes.Block;

import java.util.ArrayList;
import java.util.List;

public class AcceptMsg extends PaxosMsg{
    public int r;
    public List<Block> blocks;

    public AcceptMsg(int id, int _r, List<Block> b, String sAddr, int _round) {
        type = PaxosMassegesTypes.ACCEPT;
        serverAddr = sAddr;
        serverID = id;
        r = _r;
        blocks = b;
        round = _round;
    }
}
