package DataTypes;

public class Address {
    String _State;
    String _City;
    String _zipCode;

    Address(String State, String City,String zipCode ){
        _State = State;
        _City = City;
        _zipCode = zipCode;

    }

    Address(){
        _State = "State";
        _City = "City";
        _zipCode = "zipCode";
    }
}
