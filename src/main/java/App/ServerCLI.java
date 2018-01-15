package App;

import java.util.Random;

import DataTypes.Block;
import DataTypes.Transaction;
import Utiles.JsonSerializer;
import org.apache.commons.cli.*;


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
        options.addOption(Option.builder("show")
                .hasArg()
                .desc("shows the required block")
                .build());
        options.addOption(Option.builder("propose")
                .hasArg()
                .desc("propose a new block")
                .build());
        options.addOption(Option.builder("add")
                .hasArg()
                .desc("adding a new block")
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
//                System.out.println("Initialization of server has finished successfully");
            }
            if (in[0].equals("kill")) {
                kill();
                return "killing the server has finished successfully";
//                System.out.println("killing the server has finished successfully");
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
//            if (in[0].split("-")[0].equals("propose")) {
//                int hash = Integer.parseInt(in[0].split("-")[1]);
//                propose(hash);
//            }
            if (in[0].equals("add")) {
//                int hash = Integer.parseInt(in[0].split("-")[1]);
                 add();
                 return null;
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
//        app.init();
    }
    private void kill() {
        app.s.stopHost();
    }
    private String showBC() {
        String res = (JsonSerializer.serialize(app.s.getBlockchain()));
        res = res.replaceAll("},", "}," + System.lineSeparator()).
                replaceAll("],", "]," + System.lineSeparator());
        return res;
    }
    private String show(int num) {
        return (JsonSerializer.serialize(app.s.getBlock(num)));
    }
    private void sleep() {
        Random ran = new Random();
        int x = ran.nextInt(3) + 3;
        app.s.sleep(x);
    }
    private void propose(int hash) {
        Block b = new Block(hash);
        System.out.println(JsonSerializer.serialize(app.s.propose(b)));
    }
    private void add() {
        Block b = new Block(0);
        Random ran = new Random();
        int tNum = ran.nextInt(10); {
            for (int i = 0 ; i < tNum ; i++) {
                b.addTransaction(new Transaction());
            }
        }
//        b.addTransaction(new Transaction());
//        b.addTransaction(new Transaction());
        app.s.addBlock(b);
    }
}
