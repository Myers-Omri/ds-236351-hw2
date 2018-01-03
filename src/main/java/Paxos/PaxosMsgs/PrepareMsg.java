package Paxos.PaxosMsgs;

public class PrepareMsg extends PaxosMsg {
    public int r;
    public PrepareMsg(int id, int _r, int b_num, String sAddr) {
        type = PaxosMassegesTypes.PREPARE;
        serverAddr = sAddr;
        serverID = id;
        blockNum = b_num;
        r = _r;
    }
}
