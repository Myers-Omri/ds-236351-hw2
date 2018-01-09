package hello;

import BlockChain.BlockChainServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static BlockChainServer server;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}