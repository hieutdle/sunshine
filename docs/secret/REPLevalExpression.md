Okay, let's see. The user has a problem where when they type "2 + 2" in the REPL, it gives an error about a missing semicolon. The current code is using the LoxMain class, which is part of a GraalVM setup. The error suggests that the parser expects a semicolon at the end of the statement.

Hmm, in the REPL loop, when the user inputs a line like "2 + 2", the code is passing that line directly to context.eval("lox", line). If the Lox language requires statements to end with a semicolon, then an expression without one would be a syntax error. But in a REPL, people often type expressions without semicolons to get the result immediately. So the problem is that the REPL isn't handling expressions as expressions; it's treating them as statements.

In many languages, the REPL automatically prints the result of expressions. For example, in JavaScript's Node.js REPL, if you type 2 + 2, it prints 4. But if you do that in a script, you need to use console.log(2+2). The REPL is more lenient.

So the solution here would be to detect if the input line is an expression and automatically wrap it in a print statement or adjust the parsing to allow expressions without semicolons in the REPL. Alternatively, modify the Lox language's grammar to allow expressions as top-level statements in the REPL context.

But how to do that in the current setup? The LoxMain class is using the GraalVM's context to evaluate the code. The error is coming from the Lox language implementation's parser, which expects statements to end with semicolons. So when the user inputs "2 + 2" without a semicolon, it's parsed as a statement, which is invalid.

One approach could be to check if the input line is an expression and add a semicolon if needed. But that might not be reliable. Alternatively, in the REPL mode, allow expressions to be entered without semicolons by treating them as print statements.

So maybe in the REPL, when the user enters a line that's an expression (not a statement), automatically wrap it in a print statement. For example, if the input is "2 + 2", transform it into "print (2 + 2);" before evaluating. But how to detect if it's an expression?

Alternatively, modify the Lox language's grammar to allow expressions as top-level elements in the REPL. But that might require changes in the Lox parser, not just the Java launcher code here.

Wait, the user's code is part of the launcher. The error comes from the Lox language's own parser. So maybe in the REPL, when the input is a single expression, the launcher can automatically add a semicolon or adjust the input to make it a valid statement.

Looking at the code: in the startEvalLoop method, each line is evaluated as a separate script. The error message says missing ';' at '<EOF>', which implies that the parser expects a semicolon after the expression. So if the user inputs "2 + 2", the parser sees it as an expression statement without the semicolon, hence the error.

The fix would be to check if the line ends with a semicolon. If not, add one. But that's a simplistic approach. Alternatively, in the REPL, automatically append a semicolon if it's missing. But that could cause issues if the line is part of a multi-line input, like a function declaration.

Wait, but the current REPL reads one line at a time. So multi-line inputs might not be handled. But for the purpose of evaluating expressions, perhaps in the REPL, each line is treated as a separate statement. So for the line "2 + 2", the launcher could append a semicolon before evaluating.

But how does the current code handle this? The line is passed directly to context.eval("lox", line). So if line is "2 + 2", the Lox parser sees an expression statement (which is allowed in Lox?), but requires a semicolon. If the user didn't include the semicolon, it's a syntax error.

So the solution would be to, in the REPL, automatically add a semicolon to the input line if it's missing. But how to do that?

In the startEvalLoop method, when reading the line, check if it ends with a semicolon. If not, append it.

Alternatively, in the REPL, allow expressions without semicolons by modifying the input to add a semicolon.

But in code:

String line = System.console().readLine();

Then, perhaps process the line before passing it to eval.

So, in the startEvalLoop:

String line = ...;

if (line is not empty and doesn't end with ';') {

line += ";";

}

context.eval("lox", line);

Wait, but that might not work if the line is part of a block. For example, if the user enters "fun foo() {", then the next line is "}", but this approach would add a semicolon, which would break.

Hmm, but the current REPL processes each line individually. So multi-line statements might not be supported. In that case, the user would have to enter the entire function in one line, which is not practical.

Alternatively, the launcher could detect if the line is a complete statement and only then evaluate it. But that's more complex.

Alternatively, in the REPL, treat each line as an expression, wrap it in a print statement. For example, if the line is "2 + 2", then modify it to "print (2 + 2);" before evaluating.

But how to differentiate between expressions and statements? For example, if the user enters "var x = 5;", that's a declaration, not an expression. So in that case, we shouldn't print it.

So perhaps in the REPL, we can check if the line parses as an expression. If yes, then print it; otherwise, execute it as a statement.

But that would require parsing the line twice: once to check if it's an expression, then again to execute. That's possible, but may have performance implications.

Alternatively, wrap the line in a print statement and see if that parses. If it does, then evaluate the wrapped version. If not, evaluate the original line.

But how to implement that? Since we are using GraalVM's polyglot context, perhaps we can't do that easily.

Alternatively, modify the Lox language's grammar to allow expressions as top-level elements in the REPL. But that would require changing the Lox language implementation, not just the launcher.

Given that the user's code is part of the launcher (LoxMain.java), maybe the solution is to, in the REPL mode, prepend "print " and append ";" to the line if it's an expression.

But how to detect if it's an expression? That's tricky. For example, "2 + 2" is an expression, but "var x = 5" is a declaration. So perhaps in the REPL, if the line starts with a keyword that introduces a statement (like var, fun, for, if, etc.), then we don't wrap it. Otherwise, we wrap it in a print statement.

But that's heuristic-based and might not cover all cases.

Alternatively, we can check if the line, when wrapped in "print (...);", parses successfully. If yes, then use that. Otherwise, use the original line.

But this approach would require parsing the line twice, once to check if it's an expression. However, in the current setup, the launcher might not have access to the parser's internals.

Alternatively, in the REPL, every line that is not a statement (i.e., doesn't end with a semicolon) is considered an expression, and we automatically print it.

Wait, but the error is because the line is an expression without a semicolon. So, in the REPL, if the user enters "2 + 2", the code is treated as an expression statement, which requires a semicolon. But they omitted the semicolon, causing a syntax error.

So, one possible solution is to automatically add the semicolon in the REPL.

In the startEvalLoop:

line = line.trim();

if (!line.endsWith(";")) {

line += ";";

}

But then, for the input "2 + 2", it becomes "2 + 2;", which is a valid expression statement. Then, the Lox interpreter would evaluate it, but expression statements in Lox might not produce any output. So the user would type 2 + 2, and the REPL would execute it as a statement, but not print anything. That's not helpful.

Ah, right. So the original issue is two-fold: the missing semicolon, and the fact that evaluating an expression statement doesn't output the value.

So the user wants the REPL to evaluate the expression and print the result. So even if the semicolon is added, the code would be "2 + 2;", which is a valid statement but doesn't produce output.

Therefore, the solution must not only add the missing semicolon but also wrap the expression in a print statement.

So in the REPL, the input line "2 + 2" should be transformed into "print (2 + 2);" before evaluation.

But how to do that without breaking other statements. For example, if the user inputs "var x = 5;", we don't want to wrap that in a print statement.

So the logic would be: if the line is an expression (not a statement), wrap it in a print.

But how to detect that? One approach is to check if the line, when wrapped in "print (...);", is a valid statement. If it is, then use that. But again, that requires parsing.

Alternatively, in the REPL, assume that any line that doesn't start with a keyword (like var, fun, for, if, etc.) is an expression and should be printed.

This is a heuristic. For example:

- If the line starts with "var", "fun", "class", "for", "if", "while", "print", "return", etc., then treat it as a statement and don't wrap.

- Otherwise, wrap in a print statement.

But this is not foolproof. For example, a user could have an expression that starts with a keyword, but that's unlikely.

Alternatively, the launcher could check if the line, when prefixed with "print ", forms a valid statement. But again, without access to the parser, this is hard.

An alternative approach: in the REPL mode, if the line parses as an expression, automatically print it. Otherwise, execute as a statement.

But how to implement that in the current code?

Since the user is using GraalVM's polyglot context, perhaps the Lox language implementation (not shown here) can be modified to have a REPL mode where expressions are automatically printed. But since the user's code is the launcher, maybe they can't change the language's internals.

Alternatively, in the launcher, when in REPL mode, wrap the input line in a print statement unless it's a declaration or other statement.

But again, without knowing the syntax, this is tricky.

Alternatively, the launcher can try to parse the line as an expression. If that succeeds, then wrap it in a print. If it fails, try to parse it as a statement. If that also fails, then show the error.

But implementing this would require access to the parser, which may not be feasible here.

Alternatively, in the REPL, every line that doesn't end with a semicolon is considered an expression to be printed, and a semicolon is added. If it does end with a semicolon, it's treated as a regular statement.

But that could be a simple heuristic. For example:

In the REPL:

- If the line ends with a semicolon, evaluate as-is.

- If not, check if it's an expression, then wrap in print and add semicolon.

But again, without parsing, it's hard to check if it's an expression.

Alternatively, always wrap the line in a print statement if it doesn't start with certain keywords.

Let me think of an example. For the input:

> 2 + 2

We could transform it into "print (2 + 2);" and evaluate that.

For input:

> var x = 5

We would leave it as "var x = 5;" (after adding semicolon) and evaluate.

But how to detect that "var x =5" is a declaration?

The heuristic could be: if the line starts with a keyword that introduces a declaration or statement (var, fun, class, for, if, while, return, print), then don't wrap in print. Otherwise, wrap in print.

So in code:

String line = ...;

line = line.trim();

if (line.isEmpty()) {

// do nothing

} else {

boolean isStatement = false;

String[] statementKeywords = {"var", "fun", "class", "for", "if", "while", "return", "print"};

for (String keyword : statementKeywords) {

if (line.startsWith(keyword + " ")) {

isStatement = true;

break;

}

}

if (!isStatement) {

// Check if it ends with a semicolon

if (line.endsWith(";")) {

// It's a statement, leave as is

} else {

line = "print " + line + ";";

}

} else {

// Add semicolon if missing

if (!line.endsWith(";")) {

line += ";";

}

}

}

Then evaluate line.

This is a heuristic approach. It may not cover all cases, but for a REPL, it might be sufficient.

For example:

Input "2 + 2" becomes "print 2 + 2;".

Input "var x = 5" becomes "var x = 5;".

Input "x = 5" (assignment expression) would be wrapped in print, so "print x =5;" which may not be desired, but assignments are expressions in many languages and would evaluate to the assigned value, so printing it might be okay.

But in Lox, assignment is an expression, so "x = 5" as an expression would return 5, so printing it would show 5.

But if the user inputs "x = 5;", the line ends with a semicolon, so it's treated as a statement and not wrapped. But in that case, the assignment happens, but nothing is printed. So the user would need to type "x =5" (without semicolon) to see the result.

Hmm, but maybe in the REPL, the user expects all expression results to be printed, regardless of semicolon. But that's a design decision.

Alternatively, in the REPL, any line that is an expression (with or without semicolon) is printed. But statements are executed without printing.

But how to differentiate.

Alternatively, for the REPL:

- If the line parses as an expression, print its value.

- If it parses as a statement, execute it.

But without access to the parser, this is difficult.

So back to the original code. The user's launcher is passing the line directly to the Lox interpreter. The error is a syntax error because "2 + 2" is parsed as an expression statement, which requires a semicolon.

So the immediate fix is to add a semicolon to the input line in the REPL.

So in the startEvalLoop, after reading the line, check if it ends with a semicolon. If not, append one.

But then, the line "2 + 2" becomes "2 + 2;", which is a valid statement, but doesn't print anything. So the user still wouldn't see the result.

Ah, right. So the missing semicolon was just part of the problem. The other part is that expression statements don't produce output. So even with the semicolon, nothing is printed.

So the solution needs to both add the semicolon and print the result.

Therefore, the REPL needs to wrap expressions in a print statement.

So the approach would be: in the REPL, for inputs that are expressions, wrap them in a print statement.

But again, the challenge is detecting whether the input is an expression.

Alternatively, in the REPL, every line that is not a statement (i.e., doesn't start with a keyword) is wrapped in a print.

So, modifying the code in startEvalLoop:

After reading the line, check if it starts with a keyword. If not, prepend "print " and append ";".

For example:

line = line.trim();

if (line starts with any statement keyword) {

if it doesn't end with ;, add it.

} else {

wrap in print and add ;

}

But how to implement this.

In code:

String line = System.console().readLine().trim();

if (line.isEmpty()) {

continue;

}

List<String> statementKeywords = List.of("var", "fun", "class", "for", "if", "while", "return", "print");

boolean isStatement = false;

for (String keyword : statementKeywords) {

if (line.startsWith(keyword + " ") || line.equals(keyword)) {

isStatement = true;

break;

}

}

if (!isStatement) {

if (!line.endsWith(";")) {

line = "print (" + line + ");";

} else {

line = "print (" + line.substring(0, line.length() -1) + ");";

}

} else {

if (!line.endsWith(";")) {

line += ";";

}

}

Then evaluate line.

But this has issues. For example, if the line is "print "Hello";", it would be considered a statement (since it starts with "print "), and add a semicolon, resulting in "print "Hello";;", which is a syntax error.

Hmm, that's a problem. So the heuristic approach is not reliable.

Alternatively, check if the line starts with a keyword followed by a space or parenthesis or something. But that might not be accurate.

Alternatively, the code could check if the line is a valid expression when wrapped in a print statement. But again, that would require parsing.

Given the time constraints and the fact that this is a REPL for a language that's presumably being developed, maybe a simple heuristic is acceptable.

So, in the startEvalLoop method:

After reading the line, if it doesn't start with a statement keyword, and doesn't end with a semicolon, then wrap it in a print statement and add a semicolon.

But even better, wrap it in print(...); to ensure proper parsing.

So:

line = line.trim();

if (line.isEmpty()) {

continue;

}

boolean isStatement = line.startsWith("var ") || line.startsWith("fun ") || line.startsWith("class ") || line.startsWith("for ") || line.startsWith("if ") || line.startsWith("while ") || line.startsWith("return ") || line.startsWith("print ");

if (!isStatement) {

if (line.endsWith(";")) {

// strip the semicolon and wrap

line = "print (" + line.substring(0, line.length() - 1) + ");";

} else {

line = "print (" + line + ");";

}

} else {

if (!line.endsWith(";")) {

line += ";";

}

}

Then evaluate this modified line.

This way, expressions are printed, and statements are executed as-is with added semicolons if needed.

Testing this:

Input: "2 + 2" → becomes "print (2 + 2);"

Input: "var x = 5" → becomes "var x =5;"

Input: "x = 5" → becomes "print (x =5);"

Input: "print "Hello"; → becomes "print "Hello";;" → syntax error.

But this is a problem. If the user enters "print "Hello"", the code would see that it starts with "print ", so it's a statement. Then, since it doesn't end with ";", add one. So the line becomes "print "Hello";" → which is correct.

Wait, if the user enters "print "Hello"", the code checks if it's a statement (yes, starts with "print "), and then adds the semicolon, resulting in "print "Hello";".

Which is correct.

Another example: the user enters "fun foo() { return 5; }" → since it starts with "fun ", it's a statement. Check if ends with ";". It does, so line remains as-is.

If the user enters "5 _ (3 + 2)", it's wrapped into "print (5 _ (3 + 2));".

But if the user enters "if (true) print "yes";", it's a statement starting with "if ", so it appends a semicolon only if missing. But the line already ends with ";", so remains "if (true) print "yes";" → correct.

This approach has potential, but it's a heuristic. However, it might handle most common cases in a REPL.

So, modifying the startEvalLoop method in the code to process the line this way before evaluating.

In the original code, the startEvalLoop reads a line and passes it directly to context.eval("lox", line). So the modification would be to process the line as per the heuristic before evaluation.

So, updating the startEvalLoop:

private void startEvalLoop(Context context) {

while (true) {

System.out.print("> ");

String line = System.console().readLine();

if (line == null) {

break;

}

line = line.trim();

if (line.isEmpty()) {

continue;

}

List<String> statementKeywords = List.of("var", "fun", "class", "for", "if", "while", "return", "print");

boolean isStatement = false;

for (String keyword : statementKeywords) {

if (line.startsWith(keyword + " ") || line.startsWith(keyword + "(") || line.equals(keyword)) {

isStatement = true;

break;

}

}

if (!isStatement) {

if (line.endsWith(";")) {

line = "print (" + line.substring(0, line.length() - 1) + ");";

} else {

line = "print (" + line + ");";

}

} else {

if (!line.endsWith(";")) {

line += ";";

}

}

try {

context.eval("lox", line);

} catch (Exception e) {

printException(e);

}

}

}

But wait, this approach wraps even lines that are already valid statements but not starting with those keywords. For example, a block statement or other constructs.

Alternatively, perhaps the Lox language allows the REPL to treat expressions as print statements. Another way could be to modify the Lox language's parser to have a special mode for REPL where a lone expression is automatically printed.

But since the user's code is in the launcher (LoxMain.java), and the Lox language's parser is part of a GraalVM language implementation (not shown here), it's possible that the launcher can't modify the parser's behavior.

Therefore, the launcher needs to modify the input line to add the print and semicolon when appropriate.

The above code attempts to do that by checking if the line starts with certain keywords. It's a heuristic but may work for common cases.

Testing this approach with the user's problem: input "2 + 2" becomes "print (2 + 2);", which is a valid statement and should print the result.

Another example: input "2 + 2;" → becomes "print (2 + 2);;", which would be a syntax error. Wait, no. Original line ends with ";", so the code would strip the semicolon and add the print. So line becomes "print (2 + 2);", which is correct.

Wait, in the code:

if (!isStatement) {

if (line.endsWith(";")) {

line = "print (" + line.substring(0, line.length() - 1) + ");";

} else {

line = "print (" + line + ");";

}

}

So if the line is "2 + 2;", then line becomes "print (2 + 2);".

Which is correct. So even if the user added a semicolon, the code converts it into a print statement.

But if the user inputs "print 42;", since it starts with "print ", it's considered a statement. The code adds a semicolon (if missing), but in this case, it already has one, so it remains "print 42;", which is correct.

Another example: input "5 _ 3" becomes "print (5 _ 3);".

Thus, this approach should handle the user's case.

But what about multi-token keywords like "else"? Well, "else" is part of an if statement, so the user wouldn't type it alone. The code checks for lines starting with the keywords followed by space or parenthesis.

This should cover most cases. It's a heuristic, but for a REPL, it's better than the current behavior.

So, the final step is to modify the startEvalLoop method in the LoxMain class to process the input line as described.
