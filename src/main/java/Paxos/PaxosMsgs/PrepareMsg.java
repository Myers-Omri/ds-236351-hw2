package Paxos.PaxosMsgs;

public class PrepareMsg extends PaxosMsg {
    public int r;
    public int promise_port;
    public PrepareMsg(int id, int _r, int b_num, String sAddr, int promise_port, int _round) {
        type = PaxosMassegesTypes.PREPARE;
        serverAddr = sAddr;
        serverID = id;
        blockNum = b_num;
        r = _r;
        round = _round;
//        port = _port;
        this.promise_port = promise_port;
    }
}
