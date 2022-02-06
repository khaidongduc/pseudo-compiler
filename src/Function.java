import java.util.ArrayList;

public class Function {
    public String functionName;
    public ArrayList<String> parameters = new ArrayList<>();
    public ArrayList<String> operations = new ArrayList<>();

    @Override
    public String toString() {
        return functionName;
    }
}
