package Utils;

import Paxos.PaxosWatcher;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;

public class LeaderFailureDetector {
    static private ZooKeeperClient zoo = null;
//    static private String ID;
    static private String root = "/ELECTION";
    static public String electedLeader = null;
//    static public boolean leaderFailure = false;
    private static Logger log = Logger.getLogger(LeaderFailureDetector.class.getName());

    static public void start(String msg) {
        try {
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
        if (electedLeader == null) {
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
            log.info(format("[%d] is the current leader", Integer.parseInt(electedLeader.split(":")[1])));
        }
    }
    static public String getCurrentLeader() {
        return electedLeader;
    }

    static public int getCurrentLeaderId() { return Integer.parseInt(electedLeader.split(":")[1]); }

    static public void close() throws InterruptedException {
        zoo.close();
    }
}


