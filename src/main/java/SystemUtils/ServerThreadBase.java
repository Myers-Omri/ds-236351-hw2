package SystemUtils;

import DataTypes.Transaction;
import Utils.JsonSerializer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class ServerThreadBase extends Thread{
    private Socket IOSocket;
    private final ServerClientBase _server;
    //private String ServerName;
    ServerThreadBase(final ServerClientBase bcServer, final Socket client) {
        super(bcServer.getName() + System.currentTimeMillis());
        this._server = bcServer;
        this.IOSocket = client;
    }

    @Override
    public void run() {
        try (
                ObjectOutputStream out = new ObjectOutputStream(IOSocket.getOutputStream());
                final ObjectInputStream in = new ObjectInputStream(IOSocket.getInputStream())) {
            MessageBase readyMessage = new MessageBase(_server.getPort(),_server.getPort(),"READY", new Transaction());
            out.writeObject(JsonSerializer.serialize(readyMessage));
            String fromClient;
            while ((fromClient = in.readLine()) != null) {
                if (JsonSerializer.deserialize(fromClient.toString(), MessageBase.class) instanceof MessageBase){
                     String msg = fromClient;
                    messageHandler(msg);
                    break;
                }
            }
            IOSocket.close();
        } catch ( IOException e) {
            e.printStackTrace();
        }
    }


    //need to be override
    private void messageHandler(String msg) {
        _server.MessageHandler(msg);
    }
}
