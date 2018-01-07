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
        Block d = consensus.propose(b);
        blockchain.add(d);
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

}
