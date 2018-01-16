package Paxos.PaxosMsgs;

public class AcceptedMsg extends PaxosMsg {
    public int r;
    public String ack;

    public AcceptedMsg(int id, int _r, String _ack, String sAddr, int _round) {
        type = PaxosMassegesTypes.ACCEPTED;
        serverAddr = sAddr;
        serverID = id;
        r =_r;
        ack = _ack;
        round = _round;
    }
}
