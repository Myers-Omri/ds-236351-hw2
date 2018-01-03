
import Paxos.LeaderFailureDetector;
import Utils.SystemUtils;

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
