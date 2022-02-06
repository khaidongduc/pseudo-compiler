import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Compiler {

    private static final ArrayList<Function> functions = new ArrayList<>();
    private static final Stack<FunctionCall> callStack = new Stack<>();
    private static Function mainFunction = null;

    /**
     * compile the file into runnable format and execute it
     *
     * @param fileName the file name
     */
    private static void compile(String fileName) {
        try {
            // remove everything before compiling
            functions.clear();
            callStack.clear();
            mainFunction = null;

            File file = new File(fileName);
            Scanner scanner = new Scanner(file);
            StringBuilder builder = new StringBuilder();
            while (scanner.hasNextLine()) {
                builder.append(scanner.nextLine());
                builder.append('\n');
            }
            String fileContent = builder.toString();
            loadFile(fileContent);
            callStack.push(new FunctionCall(mainFunction, new HashMap<>()));
            execute();
        } catch (FileNotFoundException ignored) {
            // if the file does not exist, do nothing
        }
    }

    /**
     * load the file from string into a runnable format
     * by load it into the static variables
     *
     * @param fileContent the file content
     */
    private static void loadFile(String fileContent) {
        Scanner scanner = new Scanner(fileContent);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.isBlank()) continue;
            String[] tokens = line.split(" ");
            String operationType = tokens[0];

            if (operationType.equals("function")) {
                Function function = new Function();
                function.functionName = tokens[1];
                function.parameters.addAll(Arrays.asList(tokens).subList(2, tokens.length));
                while (scanner.hasNextLine()) {
                    line = scanner.nextLine();
                    if (line.isBlank()) continue;
                    if (line.equals("end_function")) break;
                    function.operations.add(line);
                }
                functions.add(function);
            }
        }
        mainFunction = functions.stream()
                .filter(function -> function.functionName.equals("main"))
                .findAny()
                .orElse(null);
        if (mainFunction == null)
            throw new RuntimeException("No main function");
    }

    /**
     * execute the runnable format of the file
     */
    private static void execute() {
        while (!callStack.empty()) {
            FunctionCall topCall = callStack.peek();

            if (topCall.isReturnedValue) {
                callStack.pop();
                if (callStack.empty()) continue;
                FunctionCall nextFunctionCall = callStack.peek();
                if (nextFunctionCall.waitToBeAssignVariable == null)
                    continue;
                nextFunctionCall.namespace.put(
                        nextFunctionCall.waitToBeAssignVariable,
                        topCall.returnedValue
                );
                nextFunctionCall.waitToBeAssignVariable = null;
                continue;
            }

            if (topCall.currentOperationIndex == topCall.function.operations.size()) {
                callStack.pop();
                FunctionCall functionCall = new FunctionCall();
                functionCall.isReturnedValue = true;
                functionCall.returnedValue = "No message";
                callStack.push(functionCall);
                continue;
            }

            String operationString = topCall.function.operations.get(topCall.currentOperationIndex);
            ++topCall.currentOperationIndex;

            Scanner scanner = new Scanner(operationString);
            scanner.useDelimiter(" ");

            String operationType = scanner.next();
            switch (operationType) {
                case "declare" -> {
                    String variableName = scanner.next();
                    topCall.namespace.put(variableName, "Default Value");

                }
                case "assign" -> {
                    String variableName = scanner.next();
                    String expression = scanner.nextLine().strip();

                    expression = expression.substring(1, expression.length() - 1);
                    Scanner expressionScanner = new Scanner(expression).useDelimiter(" ");
                    String expressionType = expressionScanner.next();
                    String value = expressionScanner.nextLine().strip();

                    if (!topCall.namespace.containsKey(variableName)) {
                        throw new RuntimeException("Variable not existed within this scope");
                    }

                    switch (expressionType) {
                        case "string" -> {
                            if (value.startsWith("\"") && value.endsWith("\"")) {
                                value = value.substring(1, value.length() - 1);
                                topCall.namespace.put(variableName, value);
                            } else {
                                throw new RuntimeException(String.format("%s is not a string", value));
                            }
                        }
                        case "var" -> {
                            topCall.namespace.put(variableName, topCall.namespace.get(value));
                        }
                        case "call" -> {
                            String[] tokens = value.split(" ");
                            String functionName = tokens[0];
                            String[] passedParameters = Arrays.copyOfRange(tokens, 1, tokens.length);

                            Function function = functions.stream()
                                    .filter(f -> f.functionName.equals(functionName))
                                    .findAny()
                                    .orElse(null);

                            HashMap<String, String> functionNamespace = new HashMap<>();
                            for (int i = 0; i < function.parameters.size(); ++i) {
                                String paramName = function.parameters.get(i);
                                String passedValue = topCall.namespace.get(tokens[i + 1]);
                                functionNamespace.put(paramName, passedValue);
                            }
                            topCall.waitToBeAssignVariable = variableName;
                            callStack.push(new FunctionCall(function, functionNamespace));
                        }
                    }
                }
                case "print" -> {
                    String expression = scanner.nextLine().strip();

                    expression = expression.substring(1, expression.length() - 1);
                    Scanner expressionScanner = new Scanner(expression).useDelimiter(" ");
                    String expressionType = expressionScanner.next();
                    String value = expressionScanner.nextLine().strip();
                    switch (expressionType) {
                        case "string" -> {
                            if (value.startsWith("\"") && value.endsWith("\"")) {
                                value = value.substring(1, value.length() - 1);
                                System.out.println(value);
                            } else {
                                throw new RuntimeException(String.format("%s is not a string", value));
                            }
                        }
                        case "var" -> {
                            String printedValue = topCall.namespace.get(value);
                            if (printedValue == null) {
                                throw new RuntimeException();
                            }
                            System.out.println(printedValue);
                        }
                        case "call" -> {
                            throw new RuntimeException("This feature is not supported\n" +
                                    "please print from string or a variable");
                        }
                    }
                }
                case "call" -> {
                    String expression = scanner.nextLine().strip();

                    String[] tokens = expression.split(" ");
                    String functionName = tokens[0];
                    Function function = functions.stream()
                            .filter(f -> f.functionName.equals(functionName))
                            .findAny()
                            .orElse(null);
                    HashMap<String, String> functionNamespace = new HashMap<>();
                    for (int i = 0; i < function.parameters.size(); ++i) {
                        String paramName = function.parameters.get(i);
                        String passedValue = topCall.namespace.get(tokens[i + 1]);
                        functionNamespace.put(paramName, passedValue);
                    }
                    callStack.push(new FunctionCall(function, functionNamespace));
                }
                case "return" -> {
                    String expression = scanner.nextLine().strip();

                    expression = expression.substring(1, expression.length() - 1);
                    Scanner expressionScanner = new Scanner(expression).useDelimiter(" ");
                    String expressionType = expressionScanner.next();
                    String value = expressionScanner.nextLine().strip();

                    FunctionCall functionCall = new FunctionCall();
                    functionCall.isReturnedValue = true;

                    switch (expressionType) {
                        case "string" -> {
                            if(value.startsWith("\"") && value.endsWith("\"")) {
                                functionCall.returnedValue = value.substring(1, value.length() - 1);
                            } else {
                                throw new RuntimeException(String.format("%s is not a string", value));
                            }
                        }
                        case "var" -> {
                            functionCall.returnedValue = topCall.namespace.get(value);
                        }
                        case "call" -> {
                            throw new RuntimeException("This feature is not supported\n" +
                                    "please return from string or a variable");
                        }
                    }
                    callStack.pop(); // the function goes out of the stack after return
                    callStack.push(functionCall);
                    // push the return value on the top to be collected by
                    // the function called it
                }
            }
        }
    }

    public static void main(String[] args) {
        compile("src\\test.txt");
    }
}
