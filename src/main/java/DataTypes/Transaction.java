package DataTypes;


public class Transaction {
    public enum TransactionType {
        INIT_SHIPMENT,
        LOCATION_UPDATE,
        ARRIVED,
        COLLECTED_BY_CUSTOMER,
        DEFAULT
    }

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

    public Transaction(int transaction_id, int item_id, int client_id, TransactionType type ){
        _transactionId = transaction_id;
        _itemId = item_id;
        _clientId = client_id;
        _type = type;
    }

    public Transaction(int transaction_id){
        _transactionId = transaction_id;
    }
    public Transaction(){
        _transactionId = 0;
        _itemId = 0;
        _clientId = 0;
        _type = TransactionType.DEFAULT;
    }

    @Override
    public String toString() {
        return Integer.toString(_transactionId);
    }

    //double data = random();


}
