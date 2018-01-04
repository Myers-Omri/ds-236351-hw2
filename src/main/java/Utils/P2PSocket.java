package Utils;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;

public class P2PSocket {
    private static Logger log = Logger.getLogger(P2PSocket.class.getName());
    private int port;
    List<String> msgs = new ArrayList<>();
    private ServerSocket socket;
    private Socket concc;
    private final Object lock = new Object();
    boolean alive = true;
    Thread listener;
     public P2PSocket(int _port) throws IOException {
         port = _port;
         socket = new ServerSocket(port);
     }

     public void start() {
         listener = new Thread() {
             @Override
             public void run() {
//                 log.info(String.format("P2P socket started on [%s]", new String(socket.getInetAddress().getAddress())));
                    while (alive) {
                        try {
                            concc = socket.accept();
//                            log.info(String.format("P2P socket accepted on port [%d]", port));
                            BufferedReader in = new BufferedReader(new InputStreamReader(concc.getInputStream()));
                            readAll(in);
                            concc.close();
                        } catch (IOException e) {
                            alive = false;
                            log.info(format("socket on port [%s - %s] has closed",
                                    concc.getLocalAddress().toString(),
                                    concc.getRemoteSocketAddress().toString()));
                        }
                        if (msgs.size() > 0) {
                             synchronized(lock) {
                                 lock.notify();
                             }
                         }
                    }
             }
         };
         listener.start();
     }

    public List<String> getMsgs() {
         while (msgs.size() == 0) {
             try {
                 synchronized(lock) {
                     lock.wait();
                 }
             } catch (InterruptedException e) {
                 try {
                     concc.close();
                     socket.close();
                 } catch (IOException e1) {
                     e1.printStackTrace();
                 }
                 return Collections.emptyList();
             }
         }
         List<String> ret = msgs;
         msgs = Collections.emptyList();
        return ret;
    }

    public void close() {
        alive = false;
        try {
            concc.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        listener.interrupt();
    }

    private void readAll(BufferedReader b)  {
         String line;
         try {
             while ((line = b.readLine()) != null) {
                 msgs.add(line);
             }
         } catch (Exception e) {
             log.debug("Exception while reading a socket: " + e.getMessage());
         }

    }
}
