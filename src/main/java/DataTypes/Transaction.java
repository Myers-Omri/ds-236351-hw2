package DataTypes;


public class Transaction {
    public enum TransactionType {
        INIT_SHIPMENT,
        LOCATION_UPDATE,
        ARRIVED,
        COLLECTED_BY_CUSTOMER,
        DEFAULT
    }

    private TransactionType _type = TransactionType.DEFAULT;
    private int transactionId;
    private int clientId; //external client- the one that uses ds-shipping service
    private int receiverId;
    private int itemId;
    public int officeId; //internal client- the one that update the BlockChain for parcel location.
    public String serviceId;

     // type dependent fields:
    // for INIT_SHIPMENT:
    public String from;
    public String to;
    public String description;

    // for LOCATION_UPDATE
    public String currentLocation;
    public String nextHop;

     @Override
    public String toString() {
        return ("[ID=" + Integer.toString(transactionId) + ", Item ID=" + Integer.toString(itemId) + ", From ID=" + Integer.toString(clientId) + ", To ID=" + Integer.toString(receiverId) + "]");
    }

    // all getters:
    public int getClientId() {
        return clientId;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public int getItemId() {
        return itemId;
    }

    public int getOfficeId() {
        return officeId;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getDescription() {
        return description;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public String getNextHop() {
        return nextHop;
    }

    // =================================================================

    // all setters:
    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
        this.serviceId = Integer.toString(clientId ) + "_" + Integer.toString(transactionId);
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public void setOfficeId(int officeId) {
        this.officeId = officeId;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public void setNextHop(String nextHop) {
        this.nextHop = nextHop;
    }

    // =================================================================


}
