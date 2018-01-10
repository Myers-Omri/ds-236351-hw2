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
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.String.format;

public class P2PSocket {
    private static Logger log = Logger.getLogger(P2PSocket.class.getName());
    private int port;
    private List<String> msgs = new ArrayList<>();
    private ServerSocket socket;
//    private Socket concc;
//    private final Object sync = new Object();
    private boolean alive = true;
    private Thread listener;
     public P2PSocket(int _port) throws IOException {
         port = _port;
         socket = new ServerSocket(port);
     }
//    private final Semaphore sem = new Semaphore(0, true);
//    private Lock global = new ReentrantLock();
    private final Lock lock = new ReentrantLock();
    final Condition notEmpty = lock.newCondition();

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
                                    concc.close();
                                    } catch (IOException e) {
                                        log.info("[Exception] ",e);
                                    }
                                }
                            }.start();
                        }catch (IOException e) {
                            log.info("[Exception] ",e);
                        }
                    }
             }
         };
         listener.start();
     }

    public List<String> getMsgs() {
        lock.lock();
        List<String> ret = new ArrayList<>();
        try {
         while (msgs.size() == 0) {
             notEmpty.await();
         }
         ret = msgs;
         msgs = new ArrayList<>();
         } catch (InterruptedException e) {
            log.info("[Exception] ",e);
        } finally {
            lock.unlock();
        }
        return ret;
    }

    public void close() {
        alive = false;
        try {
            socket.close();
        } catch (IOException e) {
            log.info("[Exception] ",e);
        }

        listener.interrupt();
    }

    private void readAll(BufferedReader b)  {
        lock.lock();
         String line;
         try {
             while ((line = b.readLine()) != null) {
                 msgs.add(line);
             }
//             synchronized (notEmpty) {
                 notEmpty.signalAll();
//             }
         } catch (Exception e) {
             log.info("[Exception] ",e);
         } finally {
             lock.unlock();
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

