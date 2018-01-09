package Paxos;

import Paxos.PaxosMsgs.PaxosMassegesTypes;

import java.util.HashMap;
import java.util.Map;

public class Peer {
    public int id;
    public String addr;
    public Map<String, Integer> ports = new HashMap<>();


    public Peer(int id, String addr, int a_prepare, int a_accept, int a_commit, int l_promise, int l_accepted) {
        this.id = id;
        this.addr = addr;
        ports.put(PaxosMassegesTypes.PREPARE, a_prepare);
        ports.put(PaxosMassegesTypes.ACCEPT, a_accept);
        ports.put(PaxosMassegesTypes.COMMIT, a_commit);
        ports.put(PaxosMassegesTypes.PROMISE, l_promise);
        ports.put(PaxosMassegesTypes.ACCEPTED, l_accepted);
    }
}
