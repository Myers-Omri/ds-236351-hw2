package Paxos;

import Utils.LeaderFailureDetector;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

public class PaxosWatcher implements Watcher {
    @Override
    public void process(WatchedEvent watchedEvent) {
        final Event.EventType eventType = watchedEvent.getType();
        if(Event.EventType.NodeDeleted.equals(eventType)) {
            try {
                LeaderFailureDetector.electedLeader = null;
                LeaderFailureDetector.electLeader();
//                Paxos.restartPaxos();
//                LeaderFailureDetector.leaderFailure = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
