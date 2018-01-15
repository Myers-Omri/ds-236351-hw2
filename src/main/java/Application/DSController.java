package Application;

import java.util.concurrent.atomic.AtomicLong;

//import DataTypes.Transaction;
import DataTypes.Transaction;
import org.apache.log4j.Logger;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import static java.lang.String.format;

import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class DSController {

    //private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    private Transaction transaction;

    private static Logger log = Logger.getLogger(DSController.class.getName());

    public static Integer txId=0;

//                @RequestMapping("/greeting")
//    public Greeting greeting(@RequestParam(value="name", defaultValue="World") Integer name) {
//        return new Greeting(counter.incrementAndGet(),
//                String.format(template, name.toString()));
//    }


//    @RequestMapping("/greeting")
//    public String greetingForm(Model model) {
//        model.addAttribute("greeting", new Greeting());
//        return "greeting";
//    }
//
//    @PostMapping("/greeting")
//    public String greetingSubmit(@ModelAttribute Greeting greeting) {
//        return "result";
//    }

    @RequestMapping("/index")
    public String index(){
        return "index";
    }



    @RequestMapping("/transaction")
    public String transactionForm(Model model) {
        model.addAttribute("transaction", new Transaction());
        return "transaction";
    }


    @PostMapping("/transaction")
    public String transactionSubmit(@ModelAttribute Transaction transaction) {
        Application.s.processTransaction(transaction);
        return "tresult";
    }

   //check_transaction
   @RequestMapping("/check_transaction")
   public String transactionCheck(Model model) {
       model.addAttribute("transaction", new Transaction());
       return "check_transaction";
   }

    @PostMapping("/check_transaction")
    public ModelAndView transactionCheckResult (@ModelAttribute Transaction transaction) {

        Transaction t_pending = Application.s.checkPending(transaction);
        if (t_pending != null){
            ModelAndView mav = new ModelAndView("pending");
            mav.addObject("transaction", t_pending);
            return mav;
        }

        Transaction t = Application.s.checkTransaction(transaction);
        if(t == null){
            ModelAndView mav = new ModelAndView("denied");
            return mav;
//            return transactionDenied(transaction);
        }
        ModelAndView mav = new ModelAndView("confirmed");
        mav.addObject("transaction", t);
        return mav;
//        return transactionConfirmed(t);

    }
    @PostMapping("/confirmed")
    public String transactionConfirmed(@ModelAttribute Transaction transaction) {
        //Application.server.processTransaction(transaction);
        return "confirmed";
    }

    @PostMapping("/denied")
    public String transactionDenied(@ModelAttribute Transaction transaction) {
        //Application.server.processTransaction(transaction);
        return "denied";
    }


//    @RequestMapping("/transaction")
//    public void generateTransaction(@RequestParam(value="from", defaultValue="1") Integer from,
//                                    @RequestParam(value="to", defaultValue="1") Integer to,
//                                    @RequestParam(value="item", defaultValue="1") Integer item) {
//        txId++;
//        transaction = new Transaction(txId, item, from, to, Transaction.TransactionType.INIT_SHIPMENT);
//        log.info(format("Transaction received: %s", transaction.toString()));
//       Application.server.processTransaction(transaction);
//    }
}