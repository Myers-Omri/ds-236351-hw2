
import BlockChain.BlockChainServer;
import DataTypes.Block;
import Utils.Config;
import Utils.SystemUtils;

import java.io.IOException;
import java.util.Arrays;

public class app {
    public static void main(String args[]) {
        Config.init();
        BlockChainServer s = null;
        try {
            SystemUtils.init();
            s = new BlockChainServer("testServer", Config.server_addr, new Block(0), Config.server_id, Config.p_num);
            s.testOneTimePaxos();
            s.stopHost();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            s.stopHost();
        }
//        s.testCom();

        //        s.startHost();
//        s.testBC();
    }
}
