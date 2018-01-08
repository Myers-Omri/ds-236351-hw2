package SystemUtils;

import DataTypes.Transaction;

import java.io.Serializable;


public class MessageBase {

    int _senderPort;
    public int _receiverPort;
    String _type;
    Transaction _transaction;

    public MessageBase(int sender, int receiver, String type, Transaction data){
        _senderPort = sender;
        _receiverPort = receiver;
        _type = type;
        _transaction = data;
    }
    public MessageBase(int sender, int receiver, String type){
        _senderPort = sender;
        _receiverPort = receiver;
        _type = type;
        _transaction = null;
    }
    public void SetData(Transaction data){
        _transaction = data;
    }


//    @Override
//    public String toString() {
//        return String.format("{MessageBase: {sender:%d, receiver:%d, type:%s, data:%d}}", _senderPort, _receiverPort,
//                _type, _transaction._transactionId);
//    }

    public int get_senderPort(){
        return _senderPort;
    }
    public String getType(){
        return _type;
    }
    public Transaction getData(){
        return _transaction;
    }
}
