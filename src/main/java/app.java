import SystemUtils.SystemUtils;
import ZooKeeperClient.ZooKeeperClient;
import org.apache.zookeeper.CreateMode;

import java.io.IOException;

import static ZooKeeperClient.ZooKeeperClient.connect;

public class app {
    public static void main(String args[]) {
        SystemUtils.init();
        try {
            String path = "/myFirstZnode";
            ZooKeeperClient zoo = ZooKeeperClient.connect("localhost");
            zoo.createNode(path, new byte[] {}, CreateMode.PERSISTENT);
            if (zoo.znodeExists(path, null)) {
                zoo.createNode(path + "/son", new byte[] {}, CreateMode.PERSISTENT);
                zoo.delete(path + "/son");
            }
            zoo.delete("/myFirstZnode");
            zoo.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
