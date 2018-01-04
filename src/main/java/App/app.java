
import BlockChain.BlockChainServer;
import DataTypes.Block;
import Utils.Config;
import Utils.SystemUtils;
import java.io.IOException;


public class app {
    static private BlockChainServer s;
    public static  void init() {
        Config.init();
        try {
            s = new BlockChainServer(Config.s_name, Config.server_addr, new Block(0), Config.server_id, Config.p_num);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String args[]) {

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
