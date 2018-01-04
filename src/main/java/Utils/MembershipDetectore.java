package Utils;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MembershipDetectore {
    static private ZooKeeperClient zoo = null;
    static private String root = "/MEMBERS";
    static private List<String> members = new ArrayList<>();
    private static Logger log = Logger.getLogger(MembershipDetectore.class.getName());

    static public void start(String name) {
        try {
            connect();
            register(name);
            updateMembers();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    static public void connect() throws IOException, InterruptedException {
        String serverName = "localhost";
        if (zoo == null) {
            zoo = ZooKeeperClient.connect(serverName, new MembershipWatcher());
        }
    }

    static public void register(String msg) throws KeeperException, InterruptedException {
        if (!zoo.znodeExists(root, null)) {
            zoo.createNode(root, new byte[] {}, CreateMode.PERSISTENT);
        }
        zoo.createNode(root + "/" + msg, new byte[] {}, CreateMode.EPHEMERAL);
    }

    static public void updateMembers() throws KeeperException, InterruptedException {
        members = zoo.getChildren(root, new MembershipWatcher(), null);
    }

    static public List<String> getMembers() {
        return members;
    }

    static public void close() throws InterruptedException {
        zoo.close();
    }
}

class MembershipWatcher implements Watcher {

    @Override
    public void process(WatchedEvent watchedEvent) {
        final Event.EventType eventType = watchedEvent.getType();
        if(Event.EventType.NodeDeleted.equals(eventType)) {
            try {
                MembershipDetectore.updateMembers();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}