package hello;

import BlockChain.BlockChainServer;
import DataTypes.Block;
import DataTypes.Transaction;
import Utiles.Config;
import Utiles.SystemUtiles;
import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

import static java.lang.String.format;

@SpringBootApplication
public class Application {
    public static BlockChainServer server;
    private static Logger log = Logger.getLogger(BlockChainServer.class.getName());
    public static void main(String[] args) {
        Config.init();
        SystemUtiles.init();
        try {
               server = new BlockChainServer(Config.s_name, Config.addr, new Block(0), Config.p_num);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i=0 ; i<10 ;i++ ){
            Transaction t = new Transaction();
            t.setTransactionId(i);
            server.processTransaction(t);
            log.info(format("severs blockchain size is: %d", server.getBlockchain().size()));
            try {
                Thread.sleep(2000 );
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        SpringApplication.run(Application.class, args);
    }
}