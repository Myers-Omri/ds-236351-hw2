package App;

import BlockChain.BlockChainServer;
import DataTypes.Block;
import Utils.Config;
import Utils.SystemUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;


public class app {
    static public BlockChainServer s;
    public static  void init() {
        SystemUtils.init();
        Config.init();
        try {
            s = new BlockChainServer(Config.s_name, Config.server_addr, new Block(0), Config.server_id, Config.p_num);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String args[]) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String command;
        while ((command = br.readLine()) != null) {
            String[] argv = new String[] {command};
            new ServerCLI(argv).parse();
        }

    }
}
