package BlockChain;

import java.io.Serializable;
import java.util.List;

public class Block implements Serializable{
    private int _index;
    private int _prevIndex;
    private Long _timestamp;
    private List<String> transactions;

    public Block(int index, int prevIndex) {
        _timestamp = System.currentTimeMillis();
        _index = index;
        _prevIndex = prevIndex;
    }

    @Override
    public String toString() {
        return "Block{" +
                "index=" + _index +
                ", timestamp=" + _timestamp +
                ", transactions=" + transactions +
                '}';
    }

    public int getIndex() {
        return _index;
    }

    public long getTimestamp() {
        return _timestamp;
    }



}
