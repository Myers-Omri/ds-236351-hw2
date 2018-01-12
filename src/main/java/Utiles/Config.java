package Utiles;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.Properties;

public class Config {
    static public int p_num;
    static public String s_name;
    static public int id;
    static public String addr;
    static public int[] lPort = new int[5];
    static public int[] aPort = new int[5];


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
        String lports = props.getProperty("L_PORT");
        String aports = props.getProperty("A_PORT");
        int i = 0;
        for (String p : lports.split(",")) {
            lPort[i] = Integer.parseInt(p.replaceAll("\\s+",""));
            i++;
        }
        i = 0;
        for (String p : aports.split(",")) {
            aPort[i] = Integer.parseInt(p.replaceAll("\\s+",""));
            i++;
        }
        } catch (Exception e) {
                e.printStackTrace();
        }

    }
}
