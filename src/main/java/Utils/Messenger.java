package Utils;
import Paxos.PaxosMsgs.*;
import Paxos.Peer;
import org.apache.log4j.Logger;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import static Paxos.PaxosMsgs.PaxosMassegesTypes.*;
import static Utils.JsonSerializer.deserialize;
import static java.lang.String.format;


public class Messenger {
    private HashMap<Integer, Peer> peers = new HashMap<>();
    private P2PSocket aIn;
    private P2PSocket lIn;
    private List<String> aInBuf = new ArrayList<>();
    private List<String> lInBuf = new ArrayList<>();
    private static Logger log = Logger.getLogger(Messenger.class.getName());


    public Messenger(P2PSocket aIn, P2PSocket lIn) {
        this.aIn = aIn;
        this.lIn = lIn;
        this.lIn.start();
        this.aIn.start();
        for (String s : MembershipDetectore.getMembers()) {
         Peer newPeer = (Peer) JsonSerializer.deserialize(s, Peer.class);
         peers.put(newPeer.id, newPeer);
        }
    }

    public Object receiveAcceptorMsg(int pNum) {
//        log.info(format("start receiveAcceptorMsg on round [%d]", pNum));
        if (!aIn.isEmpty() || aInBuf.isEmpty()) {
            aInBuf.addAll(aIn.getMsgs());
        }
//        log.info(format("received [%d] massages", aInBuf.size()));
        Object ret = null;
        List<String> remove = new ArrayList<>();
        for (String msg : aInBuf) {
            if (msg.contains(PREPARE)) {
                PrepareMsg aMsg = (PrepareMsg)deserialize(msg, PrepareMsg.class);
                if (aMsg.round > pNum) continue;
                remove.add(msg);
                if (aMsg.round == pNum) {
                    log.info(format("received PREPARE massage on round [%d]", pNum));
                    ret = aMsg;
                    break;
                }
            }
            if (msg.contains(ACCEPT)) {
                AcceptMsg aMsg = (AcceptMsg)deserialize(msg, AcceptMsg.class);
                if (aMsg.round > pNum) continue;
                remove.add(msg);
                if (aMsg.round == pNum) {
                    log.info(format("received ACCEPT massage on round [%d]", pNum));
                    ret = aMsg;
                    break;
                }
            }
            if (msg.contains(COMMIT)) {
                CommitMsg aMsg = (CommitMsg)deserialize(msg, CommitMsg.class);
                if (aMsg.round > pNum) continue;
                remove.add(msg);
                if (aMsg.round == pNum) {
                    log.info(format("received COMMIT massage on round [%d]", pNum));
                    ret = aMsg;
                    break;
                }
            }
        }
        aInBuf.removeAll(remove);
        return ret;
    }

    public Object receiveLeaderMsg(int pNum) {
//        log.info(format("start receiveLeaderMsg on round [%d]", pNum));
        if (!lIn.isEmpty() || lInBuf.isEmpty()) {
            lInBuf.addAll(lIn.getMsgs());
        }
        Object ret = null;
        List<String> remove = new ArrayList<>();
        for (String msg : lInBuf) {
            if (msg.contains(PROMISE)) {
                PromiseMsg aMsg = (PromiseMsg)deserialize(msg, PromiseMsg.class);
                if (aMsg.round > pNum) continue;
                remove.add(msg);
                if (aMsg.round == pNum) {
                    log.info(format("received PROMISE massage on round [%d]", pNum));
                    ret = aMsg;
                    break;
                }
            }
            if (msg.contains(ACCEPTED)) {
                AcceptedMsg aMsg = (AcceptedMsg)deserialize(msg, AcceptedMsg.class);
                if (aMsg.round > pNum) continue;
                remove.add(msg);
                if (aMsg.round == pNum) {
                    log.info(format("received ACCEPTED massage on round [%d]", pNum));
                    ret = aMsg;
                    break;
                }
            }
        }
        lInBuf.removeAll(remove);
        return ret;
    }
    public void sendMassageToLeader(String msg, int leaderID) {
        sendMsg(msg, peers.get(leaderID).addr, peers.get(leaderID).lPort, leaderID);
    }
    private boolean isAlive(int sId) {
        for (String s : MembershipDetectore.getMembers()) {
            if (((Peer) JsonSerializer.deserialize(s, Peer.class)).id == sId) {
                return true;
            }
        }
        log.info(format("server [%d] is down, send aborted", sId));
        return false;

    }
    private void sendMsg(String msg, String host, int[] port, int sId) {
        int moved = 0;
        boolean succ = false;
        while (isAlive(sId) && !succ && moved < 5) { //TODO: BUG ALERTS
            try {
                Socket peer = new Socket(host, port[moved]);
                DataOutputStream out = new DataOutputStream (peer.getOutputStream());
                out.writeBytes(msg);
                peer.close();
                succ = true;
                log.info(format("send a massage [%s] to [%s:%d]",
                        msg.split("type")[1].split(":")[1].split(",")[0].replaceAll("\"", " "), host, port[moved]));
            } catch (Exception e) {
                log.info(format("[Exception[%s:%d]]", host, port[moved]),e);
                moved++;
            }
        }

    }

    public void broadcastToAcceptors(String msg, int excludeID) {
        List<Peer> members = new ArrayList<>();
        for (String s : MembershipDetectore.getMembers()) {
            members.add((Peer) JsonSerializer.deserialize(s, Peer.class));
        }
        for (Peer p : members) {
            if (p.id != excludeID) {
                sendMsg(msg, p.addr, p.aPort, p.id);
            }
        }
    }
    public void close() {
        aIn.close();
        lIn.close();
    }
}
