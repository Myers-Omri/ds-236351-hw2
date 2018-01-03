package Paxos;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class LeaderFailureDetector {
    static private ZooKeeperClient zoo;
    static private String ID;
    static private String root = "/ELECTION";
    static private String electedLeader;
    static public boolean leaderFailure = false;


    static public void connect() throws IOException, InterruptedException {
        String serverName = "localhost";
        zoo = ZooKeeperClient.connect(serverName, new FLDWatcher());
    }

    static public void setID(String id) {
        ID = id;
    }
    static public void propose() throws KeeperException, InterruptedException {
        if (!zoo.znodeExists(root, null)) {
            zoo.createNode(root, new byte[] {}, CreateMode.PERSISTENT);
        }
        zoo.createNode(root + "/", ID.getBytes(), CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    static public void electLeader() throws KeeperException, InterruptedException {
        List<String> children = zoo.getChildren(root, null, null);
        Collections.sort(children);
        byte[] data = new byte[] {};
        for (String leader : children) {
            data = zoo.getData(root + "/" + leader, new FLDWatcher(), null);
            if (data != null) {
                break;
            }
        }
        electedLeader = new String(data);
//        System.out.println(electedLeader);
    }
    //Add useless comment as Alon

    static public String getCurrentLeader() {
        return electedLeader;
    }

    static public void close() throws InterruptedException {
        zoo.close();
    }

    static public void restart() throws InterruptedException, IOException, KeeperException {
        close();
        connect();
        propose();
    }

}


