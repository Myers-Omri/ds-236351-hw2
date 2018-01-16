package Utiles;

import com.google.gson.Gson;

/*
just a simple jason serializer for Paxos messages
 */
public class JsonSerializer {
    static Gson gson = new Gson();
    public static String serialize(Object obj) {
        return gson.toJson(obj);

    }
    public static Object deserialize(String s, Class<?> clss) {
        return gson.fromJson(s, clss);
    }
}


