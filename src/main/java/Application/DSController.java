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

    private static Logger log = Logger.getLogger(DSController.class.getName());

    //public static Integer txId=0;

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
        log.info("transaction processed");
        return "tresult";
    }

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
        }
        ModelAndView mav = new ModelAndView("confirmed");
        mav.addObject("transaction", t);
        return mav;
    }

    @PostMapping("/confirmed")
    public String transactionConfirmed(@ModelAttribute Transaction transaction) {
        return "confirmed";
    }

    @PostMapping("/denied")
    public String transactionDenied(@ModelAttribute Transaction transaction) {
        return "denied";
    }

}