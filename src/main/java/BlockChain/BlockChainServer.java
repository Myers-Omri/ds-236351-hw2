package BlockChain;

import DataTypes.Block;
import DataTypes.Transaction;
import Paxos.Paxos;
import Paxos.PaxosMsgs.PaxosDecision;
import Utiles.*;
import Utiles.Peer;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

import static java.lang.String.format;


/*
* This class implements a BlockChain Server
 * maintain the blockChain and uses paxos the get consensus for the
 * blocks to be added to the chain
*/
public class BlockChainServer {
    public int paxsosNum = 0;
    private String name;
    private String address;
    private int pNum;
    public boolean isLeader = false;
    private List<DataTypes.Block> blockchain = new ArrayList<>();
    public Messenger msn;
    private TransactionValidator validator;
    private Block currentBlock;
    public List<PaxosDecision> decided = new ArrayList<>();
    private static Logger log = Logger.getLogger(BlockChainServer.class.getName());

    public BlockChainServer(final String name, final String address, final DataTypes.Block root,
                             int p_num) throws IOException {
        Random ran = new Random();
        int x = ran.nextInt(5) + 2; //TODO: remove for production
        log.info(format("[%d] Host will wait %d seconds before starting", Config.id, x));
        try {
            Thread.sleep(x * 1000);
        } catch (InterruptedException e) {
            log.info("[Exception] ", e);
        }
        this.name = name;
        this.address = address;
        this.pNum = p_num;
        Peer peers = new Peer(Config.id, address, Config.lPort, Config.aPort);
        MembershipDetector.start(JsonSerializer.serialize(peers), Integer.toString(Config.id));
        log.info(format("[%d] Host started Membership detector", getId()));
        blockchain.add(root);
        msn = new Messenger(new P2PSocket(Config.aPort), new P2PSocket(Config.lPort));
        LeaderFailureDetector.start(format("%d", getId()));
        log.info(format("[%d] Host started Leader Failure detector", getId()));
        try {
            Thread.sleep(5 * 1000);
            log.info(format("[%d] Host started on address [%s]", getId(), getAddress()));
        } catch (InterruptedException e) {
            log.info("[Exception] ", e);
        }

        currentBlock = new Block(1); //TODO - this is merely a placeholder, need to address block-generation-cycles
        validator = new TransactionValidator(this.blockchain);
    }
    public int getId() {return Config.id;}

    public String getName() {
        return name;
    }

    private String getAddress() {
        return address;
    }

    public List<DataTypes.Block> getBlockchain() {
        return blockchain;
    }

    // propose the block to all other blockchain servers run consensus
    // and add the decision to the local chain.
    public void addBlock(Block b) {
        Paxos consensus = new Paxos(this, null, 0, 0,  (pNum / 2) + 1, paxsosNum);
        PaxosDecision decision = consensus.propose(b);
        consensus.stopPaxos();
        decided.add(decision);
        blockchain.addAll(decision.v);
        log.info(format("severs block added #####: %d", decision.v.size()));
        paxsosNum++;
    }
    public boolean validateBlock(Block b, List<Block> bl) {
       return validator.validatePaxosChain(bl, b);
    }

    public void stopHost() {
        try {
            MembershipDetector.close();
            LeaderFailureDetector.close();
            msn.close();
        } catch (InterruptedException e) {
            log.info("[Exception] ", e);
        }
    }

    public Block getBlock(int i) {
        return blockchain.get(i);
    }

    public int getBCLength(){
        return blockchain.size();
    }

    public void sleep(int seconds) {
        try {
            log.info(format("[%d] will sleep for [%d] seconds", Config.id, seconds));
            Thread.sleep(1000 * seconds);
        } catch (InterruptedException e) {
            log.info("[Exception] ", e);
        }
    }

    // check if the transaction is valid and put it in the block
    // if not the block is dropped
    public boolean processTransaction(Transaction tx) {
        log.info(format("Transaction received by server: %s", tx.toString()));
        validator.setCurrentBlock(currentBlock);
        if (! validator.Validate(tx)){
            log.info(format("Transaction dropped by server: %s", tx.toString()));
            return false;
        }
        currentBlock.addTransaction(tx);
        if (currentBlock.isFull()){
            addBlock(currentBlock);
            cleanCurrentBlock();
        }
        return true;
    }

    // after new blocks was accepted the server starts new block
    // and the transactions are discarded
    private void cleanCurrentBlock() {
        currentBlock = new Block(paxsosNum);
        log.info(format("current block size after cleaning is: %s", currentBlock.transactions.size()));
    }

    // check if transaction with the same transaction id exists in the blockchain
    // if exists return the transaction otherwise returns null
    public Transaction checkTransaction(Transaction t){
        return (validator.findTransactionByID(t));
    }

    // check if transaction with the same transaction id exists in the current block and not
    // if exists return the transaction otherwise returns null
    public Transaction checkPending (Transaction t){
        for(Transaction tr: currentBlock.transactions){
            if (t.getTransactionId() == tr.getTransactionId()){
                return tr;
            }
        }
        return null;
    }
}
