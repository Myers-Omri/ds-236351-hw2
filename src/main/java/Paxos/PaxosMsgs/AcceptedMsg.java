package Paxos.PaxosMsgs;

public class AcceptedMsg extends PaxosMsg {
    public int r;
    public String ack;

    public AcceptedMsg(int id, int _r, String _ack, int blockn, String sAddr) {
        type = PaxosMassegesTypes.ACCEPTED;
        serverAddr = sAddr;
        serverID = id;
        blockNum = blockn;
        r =_r;
        ack = _ack;
    }
}
