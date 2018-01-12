package BlockChain;


import SystemUtils.MessageBase;
import SystemUtils.ServerClientBase;
import Utils.JsonSerializer;

// this class will handel users request in BlockChainServer
public class ClientComServer extends ServerClientBase {

    public ClientComServer(final String name, final String address, final int port){
        super(name, address, port);
    }

    @Override
    public void MessageHandler(String msg){
        MessageBase m = (MessageBase) JsonSerializer.deserialize(msg, MessageBase.class);
        System.out.print("Got Message from:" + m.get_senderPort()+"Type:" + m.getType() + "data:");
        System.out.println(m.getData());
    }

}
