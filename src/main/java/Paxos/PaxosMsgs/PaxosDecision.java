package Paxos.PaxosMsgs;

import DataTypes.Block;

import java.util.List;

public class PaxosDecision {
    public List<Block> v;
    public int lastGoodRound;
    public int lastRound;
    public int paxosNum;

    public PaxosDecision(List<Block> v, int lastGoodRound, int lastRound, int paxosNum) {
        this.v = v;
        this.lastGoodRound = lastGoodRound;
        this.lastRound = lastRound;
        this.paxosNum = paxosNum;
    }
}
