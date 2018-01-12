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

    Address(String stt){
        _State = stt;
        _City = "City";
        _zipCode = "zipCode";
    }

    @Override
    public String toString(){
        return ("[_State=" + _State + ", _City=" + _City + ", _zipCode=" + _zipCode + "]");

    }
}
