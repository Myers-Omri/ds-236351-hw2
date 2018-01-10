package Paxos.PaxosMsgs;

import DataTypes.Block;

import java.util.List;

public class CommitMsg extends PaxosMsg {
    public int r;
    public List<Block> blocks;

    public CommitMsg(int id, int _r, List<Block> b, String sAddr, int _round) {
        type = PaxosMassegesTypes.COMMIT;
        serverAddr = sAddr;
        serverID = id;
        r = _r;
        blocks = b;
        round = _round;
//        port = _port;
    }
}
