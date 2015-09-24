package performCalculationBPMN;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import java.lang.Long;


public class NumberAdder implements JavaDelegate {

    public void execute(DelegateExecution execution) throws Exception {

        int number1 = Integer.parseInt((String) execution.getVariable("number1"));
        int number2 = Integer.parseInt((String) execution.getVariable("number2"));
        int result = number1 + number2;
        execution.setVariable("result", "" + result);

    }
}