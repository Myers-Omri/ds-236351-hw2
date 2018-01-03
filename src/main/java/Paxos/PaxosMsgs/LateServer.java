package Paxos.PaxosMsgs;

public class LateServer extends PaxosMsg {

    public LateServer(int id, int b_num) {
        type = PaxosMassegesTypes.LATE_SERVER;
        serverID = id;
        blockNum = b_num;
    }
}
