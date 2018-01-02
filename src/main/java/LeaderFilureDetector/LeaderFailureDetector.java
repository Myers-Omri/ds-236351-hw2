package LeaderFilureDetector;

import ZooKeeperClient.ZooKeeperClient;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class LeaderFailureDetector {
    static private ZooKeeperClient zoo;
    static private String ID;
    static private String root = "/ELECTION";
    static private String electedLeader;


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
        String leader = children.get(0);
        byte[] data = zoo.getData(root + "/" + leader, null, null);
        electedLeader = new String(data);
        System.out.println(electedLeader);
    }

    static public String getCurrentLeader() {
        return electedLeader;
    }

}


