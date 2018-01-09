
//
//import SystemUtils.MessageBase;
//import Utils.SystemUtils;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import SystemUtils.ServerClientBase;
//
//public class app {
//
//    public static void test_BcServers() {
//        List<ServerClientBase> BsList = new ArrayList<ServerClientBase>();
//        ServerClientBase bs1 = new ServerClientBase("bs1", "127.0.0.1", 12345);
//        BsList.add(bs1);
//        bs1.startHost();
//        ServerClientBase bs2 = new ServerClientBase("bs2", "127.0.0.1", 12346);
//        BsList.add(bs2);
//        bs2.startHost();
//
//        for (int i = 0; i < 10; i++) {
//            if (i % 2 == 0) {
////                bs1.sendMessage(new MessageBase(12345, 12346, "ECHO", Integer.toString(i)));
//            } else {
////                bs2.sendMessage(new MessageBase(12346, 12345, "ECHO", Integer.toString(i)));
//            }
//
//        }
//
//        bs1.stopHost();
//        bs2.stopHost();
//    }
//
//    public static void main(String args[]) {
//        SystemUtils.init();
//        try {
////            LeaderFailureDetector.connect();
////            LeaderFailureDetector.setID("234.12.12.2");
////            LeaderFailureDetector.propose();
////            LeaderFailureDetector.electLeader();
//
//            test_BcServers();
//            //while(true) {}
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
>>>>>>> origin
