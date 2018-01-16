package BlockChain;

import DataTypes.Block;
import DataTypes.Transaction;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;

public class TransactionValidator {
    private List<Block> blockchain = new ArrayList<>();
    private Block currentBlock;
    private Transaction currentTransaction;
    private static Logger log = Logger.getLogger(Block.class.getName());

    public TransactionValidator(List<Block> blockchainRef){
        blockchain = blockchainRef;
        currentBlock = null;
        currentTransaction = null;
    }

    public void setCurrentBlock(Block currentBlock) {
        this.currentBlock = currentBlock;
    }

    public boolean Validate(Transaction currentTransaction){
        this.currentTransaction = currentTransaction;
        for (Transaction t : currentBlock.transactions){
            if (!(validateIds(t) && validateFromTo(t))){
                return false;
            }
        }

        for (Block b : blockchain){
            for (Transaction t : b.transactions){
                if (!(validateIds(t) && validateFromTo(t))){
                    return false;
                }
            }
        }
        return true;
    }

    private boolean validateFromTo(Transaction t) {
        boolean res = true;
        if (Objects.equals(t.serviceId, currentTransaction.serviceId)){
            res = (Objects.equals(t.getFrom(), currentTransaction.getFrom())) &&
                    (Objects.equals(t.getTo(), currentTransaction.getTo()));
        }
        if (! res){
            log.info(format("(validateFromTo) Invalid transaction ", t.toString()));
        }
        return res;
    }

    private boolean validateIds(Transaction t) {
        boolean res = true;

        if (t.getTransactionId() == currentTransaction.getTransactionId()){
            res =  (t.getClientId() == currentTransaction.getClientId()) &&
                    (t.getItemId() == currentTransaction.getItemId());
        }

        if (Objects.equals(t.serviceId, currentTransaction.serviceId)){
            res = res &&  (t.getClientId() == currentTransaction.getClientId()) &&
                    (t.getItemId() == currentTransaction.getItemId() );
        }
        if (! res){
            log.info(format("(validateIds) Invalid transaction ", t.toString()));
        }
        return res; //the transaction id is diffrent so no problem
    }

    public boolean validatePaxosChain(List<Block> tempchain, Block currentBlock) {
        for (Transaction currentTransaction : currentBlock.transactions) {
            for (Block b : tempchain) {
                for (Transaction t: b.transactions) {
                    if (!(validateIds(t) && validateFromTo(t))){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public Transaction findTransactionByID(Transaction currentTransaction) {
        for (Block b : blockchain) {
            for (Transaction t : b.transactions) {
                if (t.getTransactionId() == currentTransaction.getTransactionId()) {
                    log.info(format("found equal id %d:", t.getTransactionId()));
                    return t;
                }
            }
        }
        return null;
    }
}
