package SystemUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ServerClientBase {
    private static final String LOCAL_HOST  = "127.0.0.1";

    protected String name;
    protected String address;
    protected int port;

    private ServerSocket serverSocket;
    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(10);

    private boolean listening = true;

    // for jackson
    public ServerClientBase() {
    }

    public ServerClientBase(final String name, final String address, final int port) {
        this.name = name;
        this.address = address;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }


    public void  startHost() {
        executor.execute(() -> {
            try {
                serverSocket = new ServerSocket(port);
                System.out.println(String.format("Server %s started", serverSocket.getLocalPort()));
                listening = true;
                while (listening) {
                    final ServerThreadBase thread = new ServerThreadBase(this, serverSocket.accept());
                    thread.start();
                }
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Could not listen to port " + port);
            }
        });
    }

    public void  stopHost() {
        listening = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(MessageBase msgToSend){
        String host = LOCAL_HOST;
        try (   final Socket peer = new Socket(host, msgToSend._receiverPort);
                final ObjectOutputStream out = new ObjectOutputStream(peer.getOutputStream());
                final ObjectInputStream in = new ObjectInputStream(peer.getInputStream())) {
            Object fromPeer;
            while ((fromPeer = in.readObject()) != null) {
                if (fromPeer instanceof MessageBase) {
                    final MessageBase msg = (MessageBase) fromPeer;
                    out.writeObject(msgToSend);
                    break;
                }
            }
        }
        catch (UnknownHostException e) {
            System.err.println(String.format("Unknown host %s %d", host, port));
        }
        catch (IOException e) {
            System.err.println(String.format("%s couldn't get I/O for the connection to %s. Retrying...%n", getPort(), port));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // put here all the logic for dealing with messages need to be override
    public void MessageHandler(MessageBase msg){
        System.out.println(String.format("%d received: %s", port, msg.toString()));
    }
}
