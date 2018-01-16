package Application;

import java.util.Random;

import DataTypes.Block;
import DataTypes.Transaction;
import Utiles.JsonSerializer;
import Utiles.LeaderFailureDetector;
import org.apache.commons.cli.*;

import static java.lang.String.format;

/*
standard cli interface to interact with server
 */
public class ServerCLI {
    private String[] args = null;
    private Options options = new Options();

    public ServerCLI(String[] args) {
        this.args = args;
        options.addOption("help", "print this message");
        options.addOption("start", "start the server");
        options.addOption("kill", "kill the server");
        options.addOption("showBC", "print the whole BC");
        options.addOption("exit", "exit the program");
        options.addOption("sleep", "sleep random time");
        options.addOption("add", "adding a new block");
        options.addOption("leader", "showing how it leader");
        options.addOption(Option.builder("show")
                .hasArg()
                .desc("shows the required block")
                .build());
        options.addOption(Option.builder("propose")
                .hasArg()
                .desc("propose a new block")
                .build());
    }

    public String parse() {
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args);
            String[] in = line.getArgs();
            if (in[0].equals("help")) {
                help();
                return null;
            }
            if (in[0].equals("start")) {
                start();
                return "Initialization of server has finished successfully";
            }
            if (in[0].equals("kill")) {
                kill();
                return "killing the server has finished successfully";
            }
            if (in[0].equals("exit")) {
                exit();
                return null;
            }
            if (in[0].equals("showBC")) {
                return showBC();
            }
            if (in[0].equals("sleep")) {
                sleep();
                return null;
            }
            if (in[0].split("-")[0].equals("show")) {
                int num = Integer.parseInt(in[0].split("-")[1]);
                return show(num);
            }
            if (in[0].equals("add")) {
                 add();
                 return null;
            }
            if (in[0].equals("leader")) {
                return format("Current leader is [%d]", LeaderFailureDetector.getCurrentLeaderId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }
    private void help() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("BC server", options);
    }
    private void exit() {
        System.exit(0);
    }
    private void start() {
    }
    private void kill() {
        Application.s.stopHost();
    }
    private String showBC() {
        String res = (JsonSerializer.serialize(Application.s.getBlockchain()));
        res = res.replaceAll("},", "}," + System.lineSeparator()).
                replaceAll("],", "]," + System.lineSeparator());
        return res;
    }
    private String show(int num) {
        return (JsonSerializer.serialize(Application.s.getBlock(num)));
    }
    private void sleep() {
        Random ran = new Random();
        int x = ran.nextInt(3) + 3;
        Application.s.sleep(x);
    }

    private void add() {
        Block b = new Block(0);
        Random ran = new Random();
        int tNum = ran.nextInt(10); {
            for (int i = 0 ; i < tNum ; i++) {
                b.addTransaction(new Transaction());
            }
        }
        Application.s.addBlock(b);
    }
}
