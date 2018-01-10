package Paxos.PaxosMsgs;

public class PrepareMsg extends PaxosMsg {
    public int r;
    public PrepareMsg(int id, int _r, String sAddr, int _round) {
        serverID = id;
        type = PaxosMassegesTypes.PREPARE;
        serverAddr = sAddr;
        r = _r;
        round = _round;

    }
}
