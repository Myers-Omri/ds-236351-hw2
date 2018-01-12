package Utiles;

import Paxos.PaxosWatcher;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.String.format;

public class LeaderFailureDetector {
    static private ZooKeeperClient zoo = null;
//    static private String ID;
    static private String root = "/ELECTION";
    static public String electedLeader = null;
//    static public boolean leaderFailure = false;
    static private final Lock lock = new ReentrantLock();
    private static Logger log = Logger.getLogger(LeaderFailureDetector.class.getName());

    static public void start(String msg) {
        try {
            electedLeader = msg;
            connect();
            propose(msg);
            electLeader();
        } catch (Exception e) {
            log.info("[Exception] ",e);
        }
    }
    static public void connect() throws IOException, InterruptedException {
        String serverName = "localhost";
        if (zoo == null) {
            zoo = ZooKeeperClient.connect(serverName, new PaxosWatcher());
        }
    }

    static public void propose(String msg) throws KeeperException, InterruptedException {
        if (!zoo.znodeExists(root, null)) {
            zoo.createNode(root, new byte[] {}, CreateMode.PERSISTENT);
        }
        zoo.createNode(root + "/", msg.getBytes(), CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    static public void electLeader() throws KeeperException, InterruptedException {
        lock.lock();
        List<String> children = zoo.getChildren(root, null, null);
        Collections.sort(children);
        byte[] data = new byte[] {};
        for (String leader : children) {
            data = zoo.getData(root + "/" + leader, new PaxosWatcher() , null);
            if (data != null) {
                break;
            }
        }
        electedLeader = new String(data);
        log.info(format("[%d] is the current leader", Integer.parseInt(electedLeader)));
        lock.unlock();
    }
//    static public String getCurrentLeader() {
//        return electedLeader;
//    }

    static public int getCurrentLeaderId() {
        lock.lock();
        int leaderID = Integer.parseInt(electedLeader);
        lock.unlock();
        return leaderID;
    }

    static public void close() throws InterruptedException {
        zoo.close();
    }
}


