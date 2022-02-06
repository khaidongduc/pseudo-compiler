Author: Khai Dong \
This is a pseudo compiler created for academic purpose.
The languages created has the following rules:
1. The only data type is String.
2. There must be a "main" function within a file.
3. The compiling process only happens in one file.
4. After declaration, the default value for the variable is "Default Value" as such there is no null
5. The code is interpreted.
6. There can only be one command per line. If there are multiple, only the first one would be executed.
7. The code detects through spacing and parenthesis.
8. Can not access other namespaces.
The syntax is as follows:

Further guides are as follows:
1. Declare a function
```
function <function_name> <param_1> <param_2>
<function_body>
end_function
```
2. Supported commands
   1. print: Print out the value on a line. Does not support printing directly from function call. Please assign the value to a function before printing.
   ```
    print (string "<the string>")
    print (var <variable_name>)
    ```
   2. declare: Declare a new variable. The new variable has a default value
   ```
   declare <variable_name>
   ```
   3. call: Call a function with parameters
   ```
   call <function_name> <param_1> <param_2> ...
   ```
   4. assign: Assign a variable with a value. Either from an inputted string, variable, or return value of function
   ```
   assign <variable_name> (string "<string>")
   assign <variable_name> (var <variable_name>)
   assign <variable_name> (call <function_name> <params>...)
   ```
3. return: Returning out the value on a line. Does not support printing directly from function call. Please assign the value to a function before returning.
   ```
   return (string "<the string>")
   return (var <variable_name>)
    ```

A complete file is as follows:
```
function func1 param1 param2
print (var param1)
return (var param2)
end_function

function func2 param1
declare var1
assign var1 (string "var1 in func2")
declare var2
assign var2 (string "var2 in func2/return value for func1")
declare res
assign res (call func1 var1 var2)
print (var res)
return (string "Return value from func2")
end_function

function main
print (string "Printed 1st")
declare var1
print (var var1)
assign var1 (call func2 var1)
print (var var1)
end_function
```

To compile the file, call the function `Compiler.compile(filePath)` using Java14 or higher.