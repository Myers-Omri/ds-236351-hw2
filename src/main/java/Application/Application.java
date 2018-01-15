package Application;

import BlockChain.BlockChainServer;
import DataTypes.Block;
import Utiles.Config;
import Utiles.SystemUtiles;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

import static java.lang.String.format;

@SpringBootApplication
public class Application {
    static public BlockChainServer s;
    static private void init() throws IOException {
        Config.init();
        SystemUtiles.init();
        s = new BlockChainServer(Config.s_name, Config.addr, new Block(0), Config.p_num);
    }
    public static void main(String[] args) throws IOException {
        init();
        Thread thread = new Thread(){
            public void run(){
                Terminal term = Terminal.getInstance();
                term.open(0, 0, 700, 700);
            }
        };
        thread.start();
        SpringApplication.run(Application.class, args);
    }
}