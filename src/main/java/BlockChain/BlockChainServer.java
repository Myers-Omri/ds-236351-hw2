package BlockChain;

import DataTypes.Block;
import Paxos.Paxos;
import Paxos.PaxosMsgs.PaxosMassegesTypes;
import Utils.*;
import Paxos.Peer;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static java.lang.String.format;

public class BlockChainServer {
    private String name;
    private String address;
    private int pNum;
    private List<DataTypes.Block> blockchain = new ArrayList<>();
    private int id;
    Paxos consensus = null;

    public Map<String, P2PSocket> p2pSockets = new HashMap<>();
    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(10);
    private ServerSocket clientSocket;

    private boolean listening = true;
    private static Logger log = Logger.getLogger(BlockChainServer.class.getName());
    // for jackson
//    public BlockChainServer() {
//    }

    public BlockChainServer(final String name, final String address, final DataTypes.Block root,
                             int id, int p_num) throws IOException {
        Random ran = new Random();
        int x = ran.nextInt(3);
        log.info(format("[%d] Host will wait %d seconds before starting", getId(), x));
        try {
            Thread.sleep(x * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.name = name;
        this.address = address;
        this.pNum = p_num;
        this.id = id;
        Peer peers = new Peer(id, address, Config.a_prepare, Config.a_accept, Config.a_commit, Config.l_promise, Config.l_accepted);
        MembershipDetectore.start(JsonSerializer.serialize(peers), Integer.toString(Config.id));
        log.info(format("[%d] Host started Membership detector", getId()));
        blockchain.add(root);
        p2pSockets.put(PaxosMassegesTypes.COMMIT, new P2PSocket(Config.a_commit));
        p2pSockets.put(PaxosMassegesTypes.ACCEPT, new P2PSocket(Config.a_accept));
        p2pSockets.put(PaxosMassegesTypes.PREPARE, new P2PSocket(Config.a_prepare));
        p2pSockets.put(PaxosMassegesTypes.ACCEPTED, new P2PSocket(Config.l_accepted));
        p2pSockets.put(PaxosMassegesTypes.PROMISE, new P2PSocket(Config.l_promise));
        for (String s : p2pSockets.keySet()) {
            p2pSockets.get(s).start();
            log.info(format("[%d] Host started P2P socket [%s] on port [%d]", getId(), s, peers.ports.get(s)));
        }
        LeaderFailureDetector.start(format("%s:%d", getAddress(), getId()));
        log.info(format("[%d] Host started Leader Failure detector", getId()));
        if (consensus == null) {
            consensus = new Paxos(this, (pNum / 2) + 1);
            log.info(format("[%d] Host initiate consensus", getId()));
        }
        try {
            Thread.sleep(3 * 1000);
            log.info(format("[%d] Host started on address [%s]", getId(), getAddress()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        listening = true;
    }
    public void clearSocket(String s) {
        p2pSockets.get(s).clear();
    }
    public int getId() {return id;}
    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public List<DataTypes.Block> getBlockchain() {
        return blockchain;
    }
    public void addBlock(Block b) {
        List<Block> bl = consensus.propose(b);
        blockchain.addAll(bl);
    }
    public boolean validateBlock(Block b, List<Block> bl) {
        return true;
    }
    public void stopHost() {
        consensus.stopPaxos();
        try {
            MembershipDetectore.close();
            LeaderFailureDetector.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        listening = false;
            for (P2PSocket s : p2pSockets.values()) {
                s.close();
            }
    }
    public Block getBlock(int i) {
        return blockchain.get(i);
    }
    public void broadcast(String msg, String type) {
        List<Peer> members = new ArrayList<>();
        for (String s : MembershipDetectore.getMembers()) {
//            log.info(format("[%d] see [%s] as member", Config.id, s));
            members.add((Peer) JsonSerializer.deserialize(s, Peer.class));
        }
        for (Peer p : members) {
            sendMessage(msg, p.addr, p.ports.get(type));
        }
//        members.forEach(peer -> sendMessage(msg, peer.addr, peer.ports.get(type)));
    }

    public void sendMessage(String msg, String host, int port) {
        try {
//            String host = p.addr;
//            int port = p.ports.get(type);
            log.info(format("[%d] trying to send a massage  to (%s:%d)", getId(), host, port));
            Socket peer = new Socket(host, port);
            DataOutputStream  out = new DataOutputStream (peer.getOutputStream());
            out.writeBytes(msg);
            log.info(format("[%d] send a massage to (%s:%d)", getId(), host, port));
            peer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getBCLength(){
        return blockchain.size();
    }

    public List<String> receiveMessage(String type) {
        log.info(format("[%d] try to receive a massage on (%s:%s)", getId(), getAddress(), type));
        List<String> ret = p2pSockets.get(type).getMsgs();
        log.info(format("[%d] received a massage on (%s:%s)", getId(), getAddress(), type));
        return ret;
    }

    public Block propose(Block b) {
        return consensus.propose(b).get(0);
    }

    public void sleep(int seconds) {
        try {
            log.info(format("[%d] will sleep for [%d] seconds", Config.id, seconds));
            Thread.sleep(1000 * seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
