package DataTypes;


import java.util.Random;

public class Transaction {
    public enum TransactionType {
        INIT_SHIPMENT,
        LOCATION_UPDATE,
        ARRIVED,
        COLLECTED_BY_CUSTOMER
    }
    public int forTest;
    public TransactionType _type;
    public int _transactionId;
    public int _clientId; //external client- the one that uses ds-shipping service
    public int _itemId;
    public int _officeId; //internal client- the one that update the BlockChain for parcel location.


     // type dependent fields:
    // for INIT_SHIPMENT:
    public Address _from;
    public Address _to;
    public String _description;
    //public String _shipApproval;

    // for LOCATION_UPDATE
    public Address _currentLocation;
    public Address _nextHop;

    //for COLLECTED_BY_CUSTOMER:
    //public String _arrivedApproval;
    public Transaction() {
        Random ran = new Random();
        forTest = ran.nextInt(100);
    }
    Transaction(int transaction_id, int item_id, int client_id, TransactionType type ){
        _transactionId = transaction_id;
        _itemId = item_id;
        _clientId = client_id;
        _type = type;
    }



    //double data = random();


}
