package DataTypes;

import java.util.ArrayList;
import java.util.List;

public class Block {
    private long prevBlockHash;
    private List<Transaction> transactions;

    public Block(long hash) {
        prevBlockHash = hash;
        transactions = new ArrayList<Transaction>();
    }
}
