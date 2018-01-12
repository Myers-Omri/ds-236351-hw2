package Utiles;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.String.format;
import static java.util.concurrent.Executors.newFixedThreadPool;

public class P2PSocket {
    private static Logger log = Logger.getLogger(P2PSocket.class.getName());
    private int[] ports = new int[5];
    private List<String> msgs = new ArrayList<>();
    private ServerSocket[] sockets = new ServerSocket[5];
    private boolean alive = true;
//    private Thread listener;
    private ExecutorService tp = newFixedThreadPool(5);
     public P2PSocket(int[] _ports) throws IOException {
         for (int i = 0 ; i < 5 ; i++) {
             int port = _ports[i];
             sockets[i] = new ServerSocket(port, 200);
             sockets[i].setReuseAddress(true);
         }

     }
    private final Lock lock = new ReentrantLock();
    final Condition notEmpty = lock.newCondition();

    void handle(int i) {
        while (alive) {
            try {
                Socket concc = sockets[i].accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(concc.getInputStream()));
                readAll(in);
                concc.close();
            } catch (IOException e) {
                log.info("[Exception] in socket [i] ",e);
            }
        }
    }
    public void start() {
        tp.execute(()->handle(0));
        tp.execute(()->handle(1));
        tp.execute(()->handle(2));
        tp.execute(()->handle(3));
        tp.execute(()->handle(4));
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
            for (int i = 0 ; i < 5 ; i++) {
                sockets[i].close();
            }
        } catch (IOException e) {
            log.info("[Exception] ",e);
        }

        tp.shutdownNow();
    }

    private void readAll(BufferedReader b)  {
        lock.lock();
         String line;
         try {
             while ((line = b.readLine()) != null) {
                 msgs.add(line);
             }
             notEmpty.signalAll();
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
        lock.lock();
        boolean size = (msgs.size() == 0);
        lock.unlock();
        return size;
    }
}

