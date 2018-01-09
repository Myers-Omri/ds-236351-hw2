package hello;

import java.util.concurrent.atomic.AtomicLong;

import BlockChain.BlockChainServer;
import DataTypes.Transaction;
import org.apache.commons.logging.impl.SLF4JLog;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static java.lang.String.format;

@RestController
public class GreetingController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    private Transaction transaction;

    private static Logger log = Logger.getLogger(GreetingController.class.getName());

    public static Integer txId=0;

    @RequestMapping("/greeting")
    public Greeting greeting(@RequestParam(value="name", defaultValue="World") Integer name) {
        return new Greeting(counter.incrementAndGet(),
                String.format(template, name.toString()));
    }

    @RequestMapping("/transaction")
    public void generateTransaction(@RequestParam(value="from") Integer from,
                                    @RequestParam(value="to") Integer to,
                                    @RequestParam(value="item") Integer item) {
        txId++;
        transaction = new Transaction(txId, item, from, to, Transaction.TransactionType.INIT_SHIPMENT);
        log.info(format("Transaction received: %s", transaction.toString()));
        Application.server.processTransaction(transaction);
    }
}