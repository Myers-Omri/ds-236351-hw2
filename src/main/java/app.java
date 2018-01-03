
import BlockChain.BlockChainServer;
import DataTypes.Block;
import Paxos.LeaderFailureDetector;
import Utils.SystemUtils;

import java.lang.reflect.Array;
import java.util.Arrays;

public class app {
    public static void main(String args[]) {
        SystemUtils.init();
        BlockChainServer s = new BlockChainServer("testServer", "localhost", new Block(0), Arrays.asList("127.0.0.1"), 0);
        s.startHost();
        s.testBC();
    }
}
