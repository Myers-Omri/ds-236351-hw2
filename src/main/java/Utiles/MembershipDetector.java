package Utiles;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MembershipDetector {
    static private ZooKeeperClient zoo = null;
    static private String root = "/MEMBERS";
    static private List<String> members = new ArrayList<>();
    private static Logger log = Logger.getLogger(MembershipDetector.class.getName());

    static public void start(String data, String name) {
        try {
            connect();
            register(data, name);
//            updateMembers();
        } catch (Exception e) {
            log.info("[Exception] ",e);
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

    static public List<String> getMembers() {
        List<String> members = new ArrayList<>();
        try {
            List<String> children = zoo.getChildren(root, null, null);
            for (String leader : children) {
                members.add(new String(zoo.getData(root + "/" + leader, null , null)));
            }
        } catch (Exception e) {
            log.info("[Exception] ",e);
        }

        return members;
    }

    static public void close() throws InterruptedException {
        zoo.close();
    }
}
