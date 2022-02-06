import java.util.HashMap;

public class FunctionCall {
    public Function function;
    public int currentOperationIndex;
    public HashMap<String, String> namespace;
    public String waitToBeAssignVariable;

    public String returnedValue;
    public boolean isReturnedValue;

    public FunctionCall(){

    }

    public FunctionCall(Function function, HashMap<String, String> startingNamespace){
        this.function = function;
        this.currentOperationIndex = 0;
        this.namespace = startingNamespace;
        this.waitToBeAssignVariable = null;
        this.returnedValue = null;
        this.isReturnedValue = false;
    }

}
