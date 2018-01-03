//package BlockChain;
//
//import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.net.Socket;
//
//
//public class BlockChainServerThread extends Thread{
//    private Socket client;
//    private final BlockChainServer BcServer;
//    //private String ServerName;
//    BlockChainServerThread(final BlockChainServer bcServer, final Socket client) {
//        super(bcServer.getName() + System.currentTimeMillis());
//        this.BcServer = bcServer;
//        this.client = client;
//    }
//
//    @Override
//    public void run() {
//        try (
//                ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
//                final ObjectInputStream in = new ObjectInputStream(client.getInputStream())) {
//            BlockChainMessage message = new BlockChainMessage(BcServer.getPort(), 1234, "READY");
//            out.writeObject(message);
//            Object fromClient;
//            while ((fromClient = in.readObject()) != null) {
//                if (fromClient instanceof BlockChainMessage) {
//                    final BlockChainMessage msg = (BlockChainMessage) fromClient;
//                    System.out.println(String.format("%d received: %s", BcServer.getPort(), fromClient.toString()));
//                    //TODO: add here check message type: add new block or request for updates something like that
//                    if ("INFO_NEW_BLOCK" == msg.getType()) {
//                        if (msg.get_blocks().isEmpty() || msg.get_blocks().size() > 1) {
//                            System.err.println("Invalid block received: " + msg.get_blocks());
//                        }
//                        synchronized (BcServer) {
//                            BcServer.addBlock(msg.get_blocks().get(0));
//                        }
//                        break;
//                    } else if ("REQ_ALL_BLOCKS" == msg.getType()) {
//                        out.writeObject(new BlockChainMessage(BcServer.getPort(), 0, "RSP_ALL_BLOCKS"));
//                                //add blocks to send    .withBlocks(agent.getBlockchain())
//                                break;
//                    }
//                }
//            }
//            client.close();
//        } catch (ClassNotFoundException | IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
