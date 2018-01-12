package Utiles;

import Paxos.PaxosMsgs.PaxosMassegesTypes;

import java.util.HashMap;
import java.util.Map;

public class Peer {
    public int id;
    public String addr;
    public int[] lPort;
    public int[] aPort;


    public Peer(int id, String addr, int[] lPort, int[] aPort) {
        this.id = id;
        this.addr = addr;
        this.lPort = lPort;
        this. aPort = aPort;
    }
}
