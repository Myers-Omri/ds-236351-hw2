package BlockChain;

import java.io.Serializable;
import java.util.List;

public class BlockChainMessage implements Serializable {

    int _senderPort;
    int _receiverPort;
    String _type;
    String _data;
    List<Block> _blocks;

    BlockChainMessage(int sender, int receiver, String type){
        _senderPort = sender;
        _receiverPort = receiver;
        _type = type;
        //_blocks = blocks;
    }
    public void SetData(String data){
        _data = data;
    }

    public String getType(){
        return _type;
    }

    public List<Block> get_blocks() {
        return _blocks;
    }

    @Override
    public String toString() {
        return String.format("BlockChainMessage {sender=%d, receiver=%d, data=%s}", _senderPort, _receiverPort, _data);
    }


}
