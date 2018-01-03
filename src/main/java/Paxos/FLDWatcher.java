package Paxos;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

public class FLDWatcher implements Watcher {
    @Override
    public void process(WatchedEvent watchedEvent) {
        final Event.EventType eventType = watchedEvent.getType();
        if(Event.EventType.NodeDeleted.equals(eventType)) {
            try {
                LeaderFailureDetector.electLeader();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
