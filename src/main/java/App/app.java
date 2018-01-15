package App;

import BlockChain.BlockChainServer;
import DataTypes.Block;
import Utiles.Config;
import Utiles.SystemUtiles;
import bsh.Interpreter;
import bsh.util.GUIConsoleInterface;
import bsh.util.JConsole;
import org.apache.commons.io.input.ReaderInputStream;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import static javax.swing.JFrame.EXIT_ON_CLOSE;


public class app {
    static public BlockChainServer s;
    static private void init() throws IOException {
        Config.init();
        SystemUtiles.init();
        s = new BlockChainServer(Config.s_name, Config.addr, new Block(0), Config.p_num);
    }
    public static void main(String args[]) throws IOException {
        init();
        Terminal term = Terminal.getInstance();
        term.open(0, 0, 700, 700);
//        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//        String command;
//        while ((command = br.readLine()) != null) {
//            String[] argv = new String[] {command};
//            new ServerCLI(argv).parse();
//        }

    }
}
