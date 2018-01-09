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
    public Messenger msn;
    Paxos consensus = null;
    public Map<String, P2PSocket> p2pSockets = new HashMap<>();
    private static Logger log = Logger.getLogger(BlockChainServer.class.getName());


    public BlockChainServer(final String name, final String address, final DataTypes.Block root,
                             int p_num) throws IOException {
        Random ran = new Random();
        int x = ran.nextInt(3);
        log.info(format("[%d] Host will wait %d seconds before starting", Config.id, x));
        try {
            Thread.sleep(x * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.name = name;
        this.address = address;
        this.pNum = p_num;
        Peer peers = new Peer(Config.id, address, Config.lPort, Config.aPort);
        MembershipDetectore.start(JsonSerializer.serialize(peers), Integer.toString(Config.id));
        log.info(format("[%d] Host started Membership detector", getId()));
        blockchain.add(root);
        msn = new Messenger(new P2PSocket(Config.aPort), new P2PSocket(Config.lPort));
        LeaderFailureDetector.start(format("%s:%d", getAddress(), getId()));
        log.info(format("[%d] Host started Leader Failure detector", getId()));
        if (consensus == null) {
            consensus = new Paxos(this, (pNum / 2) + 1, getBCLength());
            log.info(format("[%d] Host initiate consensus", getId()));
        }
        try {
            Thread.sleep(5 * 1000);
            log.info(format("[%d] Host started on address [%s]", getId(), getAddress()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public int getId() {return Config.id;}
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
        try {
            consensus.stopPaxos();
            MembershipDetectore.close();
            LeaderFailureDetector.close();
            msn.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public Block getBlock(int i) {
        return blockchain.get(i);
    }

    public int getBCLength(){
        return blockchain.size();
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
