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
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.String.format;

public class P2PSocket {
    private static Logger log = Logger.getLogger(P2PSocket.class.getName());
    private int port;
    private List<String> msgs = new ArrayList<>();
    private ServerSocket socket;
//    private Socket concc;
//    private final Object lock = new Object();
    private boolean alive = true;
    private Thread listener;
     public P2PSocket(int _port) throws IOException {
         port = _port;
         socket = new ServerSocket(port);
     }
    private final Semaphore sem = new Semaphore(0, true);
//    private Lock global = new ReentrantLock();

     public void start() {
         listener = new Thread() {
             @Override
             public void run() {
//                 log.info(String.format("P2P socket started on [%s]", new String(socket.getInetAddress().getAddress())));
                    while (alive) {
                        try {
                            Socket concc = socket.accept();
                            new Thread() {
                                @Override
                                public void run() {
                                    try {
                                    BufferedReader in = new BufferedReader(new InputStreamReader(concc.getInputStream()));
                                    readAll(in);
                                    if (msgs.size() > 0) {
                                        sem.release();
                                        log.info(format("notify on port [%d]", port));

                                    }
                                    concc.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }.start();
                        }catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
             }
         };
         listener.start();
     }
//    public String getFirstMsg() {
//        while (msgs.size() == 0) {
//            try {
//                synchronized(lock) {
//                    lock.wait();
//                }
//            } catch (InterruptedException e) {
//                return null;
//            }
//        }
//        String ret = msgs.get(0);
//        msgs.remove(0);
//        return ret;
//    }

    public List<String> getMsgs() {
         while (msgs.size() == 0) {
             log.info(format("massages size is 0 on port [%d]", port));
             try {
                 sem.acquire();
             } catch (InterruptedException e) {
                 return Collections.emptyList();
             }
         }
        log.info(format("massages size is [%d] on port [%d]",msgs.size(), port));
         List<String> ret = msgs;
         msgs = new ArrayList<>();
        return ret;
    }

    public void close() {
        alive = false;
        try {
//            concc.close();
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
             e.printStackTrace();
             return;
         }
    }

    public void clear() {
         msgs = new ArrayList<>();
    }

    public boolean isEmpty() {
         boolean size = (msgs.size() == 0);
         return size;
    }
}

