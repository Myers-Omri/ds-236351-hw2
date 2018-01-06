package Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

public class Config {
    static public int p_num;
    static public String s_name;
    static public int id;
    static public String addr;
    static public int a_prepare;
    static public int a_accept;
    static public int a_commit;
    static public int l_promise;
    static public int l_accepted;

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
        a_prepare = Integer.parseInt(props.getProperty("A_PREPARE"));
        a_accept = Integer.parseInt(props.getProperty("A_ACCEPT"));
        a_commit = Integer.parseInt(props.getProperty("A_COMMIT"));
        l_promise = Integer.parseInt(props.getProperty("L_PROMISE"));
        l_accepted = Integer.parseInt(props.getProperty("L_ACCEPTED"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
