package performCalculationBPMN;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;


public class AddNumbers implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        Double number1 = (Double) execution.getVariable("number1");
        Double number2 = (Double) execution.getVariable("number2");

        Double result = number1 + number2;

        System.out.println("result of calculation is " + result);

        execution.setVariable("result", result);


    }
}