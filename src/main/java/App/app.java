package App;

import BlockChain.BlockChainServer;
import DataTypes.Block;
import Utiles.Config;
import Utiles.SystemUtiles;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class app {
    static public BlockChainServer s;
    public static  void init() {
        try {
            s = new BlockChainServer(Config.s_name, Config.addr, new Block(0), Config.p_num);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String args[]) throws IOException {
        Config.init();
        SystemUtiles.init();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String command;
        while ((command = br.readLine()) != null) {
            String[] argv = new String[] {command};
            new ServerCLI(argv).parse();
        }

    }
}
