package App;

import java.util.Random;
import java.util.logging.Logger;

import DataTypes.Block;
import DataTypes.Transaction;
import Utils.JsonSerializer;
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

    public void parse() {
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args);
            String[] in = line.getArgs();
            if (in[0].equals("help")) {
                help();
            }
            if (in[0].equals("start")) {
                start();
                System.out.println("Initialization of server has finished successfully");
            }
            if (in[0].equals("kill")) {
                kill();
                System.out.println("killing the server has finished successfully");
            }
            if (in[0].equals("exit")) {
                exit();
            }
            if (in[0].equals("showBC")) {
                showBC();
            }
            if (in[0].equals("sleep")) {
                sleep();
            }
            if (in[0].split("-")[0].equals("show")) {
                int num = Integer.parseInt(in[0].split("-")[1]);
                show(num);
            }
            if (in[0].split("-")[0].equals("propose")) {
                int hash = Integer.parseInt(in[0].split("-")[1]);
                propose(hash);
            }
            if (in[0].split("-")[0].equals("add")) {
                int hash = Integer.parseInt(in[0].split("-")[1]);
                add(hash);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void help() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("BC server", options);
    }
    private void exit() {
        System.exit(0);
    }
    private void start() {
        app.init();
    }
    private void kill() {
        app.s.stopHost();
    }
    private void showBC() {
        System.out.println(JsonSerializer.serialize(app.s.getBlockchain()));
    }
    private void show(int num) {
        System.out.println(JsonSerializer.serialize(app.s.getBlock(num)));
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
    private void add(int hash) {
        Block b = new Block(hash);
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
