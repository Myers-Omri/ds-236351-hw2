package DataTypes;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

public class Block {
    public long prevBlockHash;
    public  List<Transaction> transactions;
    private int MAX_BLOCK_SIZE = 2;
    private static Logger log = Logger.getLogger(Block.class.getName());

    public  Block(long hash) {
        prevBlockHash = hash;
        transactions = new ArrayList<Transaction>();
    }

    public void addTransaction(Transaction t) {
        log.info(format("Transaction logged in block: %s", t.toString()));
        transactions.add(t);
    }


    public boolean isFull(){
        return transactions.size() == MAX_BLOCK_SIZE;
    }
//    //private boolean validate() {
//        return true;
//    }
}
