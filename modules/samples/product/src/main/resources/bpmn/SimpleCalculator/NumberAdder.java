package performCalculationBPMN;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

import java.lang.Long;


public class NumberAdder implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        Long number1 = (Long) execution.getVariable("number1");
        Long number2 = (Long) execution.getVariable("number2");
        Long result = number1 + number2;

        execution.setVariable("result", result);

    }
}