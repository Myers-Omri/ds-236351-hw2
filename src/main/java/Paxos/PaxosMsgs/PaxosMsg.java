package Paxos.PaxosMsgs;

import DataTypes.Block;

abstract public class PaxosMsg {
    public String type;
    public int serverID;
    public String serverAddr;
//    public int port;
    public int blockNum;
}
