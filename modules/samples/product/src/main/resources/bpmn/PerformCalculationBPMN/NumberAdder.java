package performCalculationBPMN;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;


public class NumberAdder implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        Double number1 = (Double) execution.getVariable("number1");
        Double number2 = (Double) execution.getVariable("number2");

        Double result = number1 + number2;

        execution.setVariable("result", result);

    }
}