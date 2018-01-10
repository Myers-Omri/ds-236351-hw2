package Paxos.PaxosMsgs;

import DataTypes.Block;

import java.util.List;

public class CommitMsg extends PaxosMsg {
    public List<Block> blocks;

    public CommitMsg(int id, List<Block> b, String sAddr, int _round) {
        type = PaxosMassegesTypes.COMMIT;
        serverAddr = sAddr;
        serverID = id;
        blocks = b;
        round = _round;
//        port = _port;
    }
}
