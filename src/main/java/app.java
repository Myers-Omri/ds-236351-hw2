
import LeaderFilureDetector.LeaderFailureDetector;
import SystemUtils.SystemUtils;
import ZooKeeperClient.ZooKeeperClient;
import org.apache.zookeeper.CreateMode;

import java.io.IOException;

import static ZooKeeperClient.ZooKeeperClient.connect;

public class app {
    public static void main(String args[]) {
        SystemUtils.init();
        try {
            LeaderFailureDetector.connect();
            LeaderFailureDetector.setID("234.12.12.2");
            LeaderFailureDetector.propose();
            LeaderFailureDetector.electLeader();
            while(true) {}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
