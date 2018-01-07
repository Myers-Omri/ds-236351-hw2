package BlockChain;

import SystemUtils.MessageBase;
import SystemUtils.ServerClientBase;

import java.util.ArrayList;
import java.util.List;


public class BlockChainServer extends ServerClientBase{

    private List<BlockChainServer> _peers;
    private List<Block> blockchain = new ArrayList<Block>();

    public BlockChainServer(final String name, final String address, final int port, List<BlockChainServer> peers){
        super(name, address,port);
        _peers = peers;
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
            log.info(format("[%d] see [%s] as member", Config.id, s));
            members.add((Peer) JsonSerializer.deserialize(s, Peer.class));
        }
        for (Peer p : members) {
            sendMessage(msg, p.addr, p.ports.get(type));
        }
//        members.forEach(peer -> sendMessage(msg, peer.addr, peer.ports.get(type)));
    }

    @Override
    public void MessageHandler(MessageBase msg){
        if (msg.getType() == "ECHO"){
            System.out.println(String.format("%d received: %s", port, msg.toString()));
        }
    }

    public int getBCLength(){
        return blockchain.size();
    }

    public List<String> receiveMessage(String type) {
        List<String> ret = p2pSockets.get(type).getMsgs();
        log.info(format("[%d] received a massage on (%s:%s)", getId(), getAddress(), type));
        return ret;
    }
//    public String receiveSingleMsg(String type) {
//        log.info(format("[%d] try to receive a massage on (%s:%s)--", getId(), getAddress(), type));
//        String ret = p2pSockets.get(type).getFirstMsg();
//        log.info(format("[%d] received a massage on (%s:%s)--", getId(), getAddress(), type));
//        return ret;
//    }
    public Block propose(Block b) {
        return consensus.propose(b).get(0);
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
