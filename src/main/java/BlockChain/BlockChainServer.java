package BlockChain;

import DataTypes.Block;
import Paxos.Paxos;
import Utils.LeaderFailureDetector;
import Paxos.Peer;
import Utils.MembershipDetectore;
import Utils.P2PSocket;
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

    private Map<Integer, P2PSocket> p2pSockets = new HashMap<>();
    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(10);
    private ServerSocket clientSocket;

    private boolean listening = true;
    private static Logger log = Logger.getLogger(BlockChainServer.class.getName());
    // for jackson
//    public BlockChainServer() {
//    }

    public BlockChainServer(final String name, final String address, final DataTypes.Block root,
                             int id, int p_num) throws IOException {
        this.name = name;
        this.address = address;
        this.pNum = p_num;
        this.id = id;
        MembershipDetectore.start(address);
        log.info(format("[%d] Host started Membership detector", getId()));
        blockchain.add(root);
        p2pSockets.put(Peer.a_commit, new P2PSocket(Peer.a_commit));
        p2pSockets.put(Peer.a_accept, new P2PSocket(Peer.a_accept));
        p2pSockets.put(Peer.a_prepare, new P2PSocket(Peer.a_prepare));
        p2pSockets.put(Peer.l_accepted, new P2PSocket(Peer.l_accepted));
        p2pSockets.put(Peer.l_promise, new P2PSocket(Peer.l_promise));
        for (int s : p2pSockets.keySet()) {
            p2pSockets.get(s).start();
            log.info(format("[%d] Host started P2P socket [%d]", getId(), s));
        }
        LeaderFailureDetector.start(format("%s:%d", getAddress(), getId()));
        log.info(format("[%d] Host started Leader Failure detector", getId()));
        if (consensus == null) {
            consensus = new Paxos(this, (pNum / 2) + 1);
            log.info(format("[%d] Host initiate consensus", getId()));
        }
        try {
            Thread.sleep(5 * 1000);
            log.info(format("[%d] Host started on address [%s]", getId(), getAddress()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        listening = true;
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
    public void broadcast(String msg, int port) {
        MembershipDetectore.getMembers().forEach(peer -> sendMessage(msg, peer, port));
    }

    public void sendMessage(String msg, String host, int port) {
        try {
            Socket peer = new Socket(host, port);
            DataOutputStream  out = new DataOutputStream (peer.getOutputStream());
            out.writeBytes(msg);
            log.debug(format("[%d] send a massage to (%s:%d)", getId(), host, port));
            peer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getBCLength(){
        return blockchain.size();
    }

    public List<String> receiveMessage(int port) {
        log.debug(format("[%d] waits for a massage on (%s:%d)", getId(), getAddress(), port));
        return p2pSockets.get(port).getMsgs();
    }

    public Block propose(Block b) {
        return consensus.propose(b);
    }
//    public void testOneTimePaxos() {
//        DataTypes.Block b = new Block(0);
//        b.addTransaction(new TestTransaction());
//        LeaderFailureDetector.start(format("%s:%d", getAddress(), getId()));
//        Paxos p = new Paxos(this, (pNum / 2) + 1);
//        DataTypes.Block b1 = p.propose(b);
//        p.stopPaxos();
//        log.info(format("block [%d] accepted", b1.prevBlockHash));
//        try {
//            LeaderFailureDetector.close();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
}
