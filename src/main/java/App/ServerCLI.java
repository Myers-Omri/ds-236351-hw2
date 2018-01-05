package App;

import java.util.logging.Logger;

import DataTypes.Block;
import DataTypes.Transaction;
import Utils.JsonSerializer;
import org.apache.commons.cli.*;


public class ServerCLI {
    private static final Logger log = Logger.getLogger(ServerCLI.class.getName());
    private String[] args = null;
    private Options options = new Options();

    public ServerCLI(String[] args) {
        this.args = args;
        options.addOption("help", "print this message");
        options.addOption("start", "start the server");
        options.addOption("kill", "kill the server");
        options.addOption("showBC", "print the whole BC");
        options.addOption("exit", "exit the program");
        options.addOption(Option.builder("show")
                .hasArg()
                .desc("shows the required block")
                .build());
        options.addOption(Option.builder("propose")
                .hasArg()
                .desc("propose a new block")
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
                log.info("Initialization of server has finished successfully");
            }
            if (in[0].equals("kill")) {
                kill();
                log.info("killing the server has finished successfully");
            }
            if (in[0].equals("exit")) {
                exit();
            }
            if (in[0].equals("showBC")) {
                showBC();
            }
            if (in[0].split("-")[0].equals("show")) {
                int num = Integer.parseInt(in[0].split("-")[1]);
                show(num);
            }
            if (in[0].split("-")[0].equals("propose")) {
                int hash = Integer.parseInt(in[0].split("-")[1]);
                propose(hash);
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
    private void propose(int hash) {
        Block b = new Block(hash);
        b.addTransaction(new Transaction());
        b.addTransaction(new Transaction());
        System.out.println(JsonSerializer.serialize(app.s.propose(b)));
    }
}
