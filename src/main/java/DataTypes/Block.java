package DataTypes;

import java.util.ArrayList;
import java.util.List;

public class Block {
    public long prevBlockHash;
    public  List<TestTransaction> transactions;

    public Block(long hash) {
        prevBlockHash = hash;
        transactions = new ArrayList<TestTransaction>();
    }

    public void addTransaction(TestTransaction t) {
        transactions.add(t);
    }

    private boolean validate() {
        return true;
    }
}
