package Paxos.PaxosMsgs;

import DataTypes.Block;

public class PaxosMsg {
    public String type;
    public int serverID;
    public String serverAddr;
    public int round;
}
