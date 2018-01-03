package DataTypes;

public class Peer {
    public int serverID;
    public String address;
    public int prepare;
    public int promise;
    public int accept;
    public int accepted;
    public int commit;

    public Peer(int id, String add, int _prepare, int _promise, int _accept, int _accepted, int _commit) {
        serverID = id;
        address = add;
        prepare = _prepare;
        promise = _promise;
        accept = _accept;
        accepted = _accepted;
        commit = _commit;
    }
}
