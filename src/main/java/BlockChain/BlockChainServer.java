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

    private void broadcast(MessageBase msg) {
        _peers.forEach(peer -> sendMessage(msg));
    }

    @Override
    public void MessageHandler(MessageBase msg){
        if (msg.getType() == "ECHO"){
            System.out.println(String.format("%d received: %s", port, msg.toString()));
        }
    }

}
