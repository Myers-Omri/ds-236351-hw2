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

    static public void start(String data, String name) {
        try {
            connect();
            register(data, name);
//            updateMembers();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    static public void connect() throws IOException, InterruptedException {
        String serverName = "localhost";
        if (zoo == null) {
            zoo = ZooKeeperClient.connect(serverName, null);
        }
    }

    static public void register(String msg, String name) throws KeeperException, InterruptedException {
        if (!zoo.znodeExists(root, null)) {
            zoo.createNode(root, new byte[] {}, CreateMode.PERSISTENT);
        }
        zoo.createNode(root + "/" + name, msg.getBytes(), CreateMode.EPHEMERAL_SEQUENTIAL);
    }

//    static public void updateMembers() throws KeeperException, InterruptedException {
//        List<String> children = zoo.getChildren(root, new MembershipWatcher(), null);
//        for (String leader : children) {
//            members.add(new String(zoo.getData(root + "/" + leader, new MembershipWatcher() , null)));
//        }
//    }

    static public List<String> getMembers() {
        List<String> members = new ArrayList<>();
        try {
            List<String> children = zoo.getChildren(root, null, null);
            for (String leader : children) {
                members.add(new String(zoo.getData(root + "/" + leader, null , null)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return members;
    }

    static public void close() throws InterruptedException {
        zoo.close();
    }
}

//class MembershipWatcher implements Watcher {
//
//    @Override
//    public void process(WatchedEvent watchedEvent) {
//        final Event.EventType eventType = watchedEvent.getType();
//        if(Event.EventType.NodeDeleted.equals(eventType)) {
//            try {
////                MembershipDetectore.updateMembers();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//}