package Utils;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.Properties;

public class Config {
    static public int p_num;
    static public String s_name;
    static public int id;
    static public String addr;
    static public int lPort;
    static public int aPort;


    static public void init() {
        try {

        File configFile = new File(Paths.get(System.getProperty("user.dir"), "target", "classes", "config.properties").toString());
        FileReader reader = new FileReader(configFile);
        Properties props = new Properties();
        props.load(reader);
        p_num = Integer.parseInt(props.getProperty("P_NUM"));
        s_name = props.getProperty("S_NAME");
        id = Integer.parseInt(props.getProperty("ID"));
        addr = props.getProperty("ADDR");
        lPort = Integer.parseInt(props.getProperty("L_PORT"));
        aPort = Integer.parseInt(props.getProperty("A_PORT"));
        } catch (Exception e) {
                e.printStackTrace();
        }

    }
}
