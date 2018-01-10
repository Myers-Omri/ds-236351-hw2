package hello;

import BlockChain.BlockChainServer;
import DataTypes.Block;
import Utils.Config;
import Utils.SystemUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class Application {
    public static BlockChainServer server;

    public static void main(String[] args) {
        Config.init();
        SystemUtils.init();
        try {
            server = new BlockChainServer(Config.s_name, Config.addr, new Block(0), Config.id, Config.p_num);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SpringApplication.run(Application.class, args);
    }
}