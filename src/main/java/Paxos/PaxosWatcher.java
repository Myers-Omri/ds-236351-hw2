package Paxos;

import Utils.LeaderFailureDetector;
import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

public class PaxosWatcher implements Watcher {
    private static Logger log = Logger.getLogger(PaxosWatcher.class.getName());
    @Override
    public void process(WatchedEvent watchedEvent) {
        final Event.EventType eventType = watchedEvent.getType();
        if(Event.EventType.NodeDeleted.equals(eventType)) {
            try {
                LeaderFailureDetector.electLeader();
            } catch (Exception e) {
                log.info("[Exception] ",e);
            }
        }
    }
}
