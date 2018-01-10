package Paxos.PaxosMsgs;

import DataTypes.Block;

public class PaxosMsg {
    public String type;
    public int serverID;
    public String serverAddr;
//    public int port;
    public int blockNum;
    public int round;
}
