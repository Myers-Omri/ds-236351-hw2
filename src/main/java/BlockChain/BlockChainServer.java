package BlockChain;

import DataTypes.Block;
import DataTypes.TestTransaction;
import Paxos.Paxos;
import Paxos.Peer;
import org.apache.log4j.Logger;
import org.apache.log4j.net.SocketServer;
import org.apache.zookeeper.KeeperException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.stream.Stream;

public class BlockChainServer {
    private String name;
    private String address;
//    private int port;
    private List<String> peers = new ArrayList<String>();
    private List<DataTypes.Block> blockchain = new ArrayList<>();
    private int id;

    private List<ServerSocket> p2pSockets = new ArrayList<>();
    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(10);
    private ServerSocket clientSocket;

    private boolean listening = true;
    Paxos consensus;
    private static Logger log = Logger.getLogger(BlockChainServer.class.getName());
    // for jackson
//    public BlockChainServer() {
//    }

    public BlockChainServer(final String name, final String address, final DataTypes.Block root,
                            final List<String> agents, int id) {
        this.name = name;
        this.address = address;
        this.peers = agents;
        this.id = id;
        blockchain.add(root);
    }
    public int getId() {return id;}
    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

//    public int getPort() {
//        return port;
//    }

    public List<DataTypes.Block> getBlockchain() {
        return blockchain;
    }

//    Block createBlock() {
//        if (blockchain.isEmpty()) {
//            return null;
//        }
//
//        Block previousBlock = getLatestBlock();
//        if (previousBlock == null) {
//            return null;
//        }
//
//        final int index = previousBlock.getIndex() + 1;
//        final Block block = new Block(index, previousBlock.getIndex());
//        System.out.println(String.format("%s created new block %s", name, block.toString()));
//        broadcast("INFO_NEW_BLOCK", block);
//        return block;
//    }

//    void addBlock(Block block) {
//        if (isBlockValid(block)) {
//            blockchain.add(block);
//        }
//    }

    public void startHost() {
//        peers.add("127.0.01"); //TODO: TEST FOR ONE PROCESS
        consensus = new Paxos();
        executor.execute(() -> {
            try {
                p2pSockets.add(new ServerSocket(Peer.a_commit));
                p2pSockets.add(new ServerSocket(Peer.a_accept));
                p2pSockets.add(new ServerSocket(Peer.a_prepare));
                p2pSockets.add(new ServerSocket(Peer.l_accepted));
                p2pSockets.add(new ServerSocket(Peer.l_promise));
//                clientSocket = new ServerSocket(1234);
//                System.out.println(String.format("Server %s started", serverSocket.getLocalPort()));
                listening = true;
//                while (listening) {
//                    final BlockChainServerThread thread = new BlockChainServerThread(this, clientSocket.accept());
//                    thread.start();
//                }
//                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
//        broadcast("REQ_ALL_BLOCKS", null);
    }

    void stopHost() {
        listening = false;
        try {
            for (ServerSocket s : p2pSockets) {
                s.close();
            }
//            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


//    private Block getLatestBlock() {
//        if (blockchain.isEmpty()) {
//            return null;
//        }
//        return blockchain.get(blockchain.size() - 1);
//    }

//    private boolean isBlockValid(final Block block) {
//        final Block latestBlock = getLatestBlock();
//        if (latestBlock == null) {
//            return false;
//        }
//        final int expected = latestBlock.getIndex() + 1;
//        if (block.getIndex() != expected) {
//            System.out.println(String.format("Invalid index. Expected: %s Actual: %s", expected, block.getIndex()));
//            return false;
//        }
////        if (!Objects.equals(block.get(), latestBlock.getHash())) {
////            System.out.println("Unmatched hash code");
////            return false;
////        }
//        return true;
//    }

//    public void broadcast(String type, final Block block) {
//        peers.forEach(peer -> sendMessage(type, peer.getAddress(), peer.getPort(), block));
//    }
    public void broadcast(String msg, int port) {
        peers.forEach(peer -> sendMessage(msg, peer, port));
    }

    public void sendMessage(String msg, String host, int port) {
        try {
            Socket peer = new Socket(host, port);
            ObjectOutputStream out = new ObjectOutputStream(peer.getOutputStream());
            out.writeChars(msg);
            peer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getBCLength(){
        return blockchain.size();
    }

    public Stream<String> receiveMessage(int port) {
        try {
            Socket peer = new Socket(getAddress(), port);
            BufferedReader in = new BufferedReader(new InputStreamReader(peer.getInputStream()));
            return in.lines();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void testBC() {
        try {
            consensus.init(this);
            log.info("consensus initialized");
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 1 ; i < 11 ; i++) {
            DataTypes.Block b = new Block(i);
            b.addTransaction(new TestTransaction());
            log.info("starting consensus propose");
            blockchain.add(consensus.propose(b));
            log.info(String.format("Consensus finished added [%d, %d]", i, getBCLength()));
        }
        for (Block b : blockchain) {
            System.out.println(b.prevBlockHash);
        }
    }
//    private void sendMessage( String type, String host, int port, Block... blocks) {
//        try (
//                final Socket peer = new Socket(host, port);
//                final ObjectOutputStream out = new ObjectOutputStream(peer.getOutputStream());
//                final ObjectInputStream in = new ObjectInputStream(peer.getInputStream())) {
//            Object fromPeer;
//            while ((fromPeer = in.readObject()) != null) {
//                if (fromPeer instanceof BlockChainMessage) {
//                    final BlockChainMessage msg = (BlockChainMessage) fromPeer;
//                    System.out.println(String.format("%d received: %s", this.port, msg.toString()));
//                    if ("READY" == msg.getType()) {
//                        out.writeObject(new BlockChainMessage(this.port, port, type));
//                                //TODO: add blocks to send
//                    } else if ("RSP_ALL_BLOCKS" == msg.getType()) {
//                        if (!msg.get_blocks().isEmpty() && this.blockchain.size() == 1) {
//                            blockchain = new ArrayList<>(msg.get_blocks());
//                        }
//                        break;
//                    }
//                }
//            }
//        } catch (UnknownHostException e) {
//            System.err.println(String.format("Unknown host %s %d", host, port));
//        } catch (IOException e) {
//            System.err.println(String.format("%s couldn't get I/O for the connection to %s. Retrying...%n", getPort(), port));
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e1) {
//                e1.printStackTrace();
//            }
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//    }

}
