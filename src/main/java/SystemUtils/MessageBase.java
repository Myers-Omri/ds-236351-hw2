package SystemUtils;

import java.io.Serializable;
import java.util.List;



public class MessageBase implements Serializable {

    int _senderPort;
    public int _receiverPort;
    String _type;
    String _data;

    public MessageBase(int sender, int receiver, String type, String data){
        _senderPort = sender;
        _receiverPort = receiver;
        _type = type;
        _data=data;
    }
    public MessageBase(int sender, int receiver, String type){
        _senderPort = sender;
        _receiverPort = receiver;
        _type = type;
        _data="";
    }
    public void SetData(String data){
        _data = data;
    }


    @Override
    public String toString() {
        return String.format("{MessageBase: {sender:%d, receiver:%d, type:%s, data:%s}}", _senderPort, _receiverPort,
                _type, _data);
    }

    public int get_senderPort(){
        return _senderPort;
    }
    public String getType(){
        return _type;
    }
    public String getData(){
        return _data;
    }
}
