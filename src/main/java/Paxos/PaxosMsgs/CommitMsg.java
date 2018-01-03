package Paxos.PaxosMsgs;

import DataTypes.Block;

public class CommitMsg extends PaxosMsg {
    public int r;
    public Block block;

    public CommitMsg(int id, int _r, Block b, int b_num, String sAddr) {
        type = PaxosMassegesTypes.COMMIT;
        serverAddr = sAddr;
        serverID = id;
        blockNum = b_num;
        r = _r;
        block = b;
    }
}
