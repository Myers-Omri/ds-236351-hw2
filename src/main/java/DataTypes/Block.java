package DataTypes;

import java.util.ArrayList;
import java.util.List;

public class Block {
    public long prevBlockHash;
    public  List<Transaction> transactions;

    public Block(long hash) {
        prevBlockHash = hash;
        transactions = new ArrayList<Transaction>();
    }

    public void addTransaction(Transaction t) {
        transactions.add(t);
    }

    private boolean validate() {
        return true;
    }
}
