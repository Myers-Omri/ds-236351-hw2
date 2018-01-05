package Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

public class Config {
    static public int p_num;
    static public int server_id;
    static public String server_addr;
    static public String s_name;

    static public void init() {
        try {

        File configFile = new File(Paths.get(System.getProperty("user.dir"), "target", "classes", "config.properties").toString());
        FileReader reader = new FileReader(configFile);
        Properties props = new Properties();
        props.load(reader);
        p_num = Integer.parseInt(props.getProperty("P_NUM"));
        server_id = Integer.parseInt(props.getProperty("SERVER_ID"));
        server_addr = props.getProperty("SERVER_ADDR");
        s_name = props.getProperty("S_NAME");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
