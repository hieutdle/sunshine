package de.hpi.swa.lox.parser;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.bytecode.BytecodeLabel;
import com.oracle.truffle.api.bytecode.BytecodeLocal;
import com.oracle.truffle.api.bytecode.BytecodeParser;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.strings.TruffleString;

import de.hpi.swa.lox.LoxLanguage;
import de.hpi.swa.lox.bytecode.LoxBytecodeRootNodeGen;
import de.hpi.swa.lox.error.LoxParseError;
import de.hpi.swa.lox.parser.LoxParser.ArrAssignmentContext;
import de.hpi.swa.lox.parser.LoxParser.ArrayContext;
import de.hpi.swa.lox.parser.LoxParser.ArrayExprContext;
import de.hpi.swa.lox.parser.LoxParser.AssignmentContext;
import de.hpi.swa.lox.parser.LoxParser.BlockContext;
import de.hpi.swa.lox.parser.LoxParser.BreakStmtContext;
import de.hpi.swa.lox.parser.LoxParser.ComparisonContext;
import de.hpi.swa.lox.parser.LoxParser.ContinueStmtContext;
import de.hpi.swa.lox.parser.LoxParser.EqualityContext;
import de.hpi.swa.lox.parser.LoxParser.ExpressionContext;
import de.hpi.swa.lox.parser.LoxParser.FactorContext;
import de.hpi.swa.lox.parser.LoxParser.FalseContext;
import de.hpi.swa.lox.parser.LoxParser.ForStmtContext;
import de.hpi.swa.lox.parser.LoxParser.IfStmtContext;
import de.hpi.swa.lox.parser.LoxParser.Logic_andContext;
import de.hpi.swa.lox.parser.LoxParser.Logic_orContext;
import de.hpi.swa.lox.parser.LoxParser.NilContext;
import de.hpi.swa.lox.parser.LoxParser.NumberContext;
import de.hpi.swa.lox.parser.LoxParser.PrintStmtContext;
import de.hpi.swa.lox.parser.LoxParser.ProgramContext;
import de.hpi.swa.lox.parser.LoxParser.StatementContext;
import de.hpi.swa.lox.parser.LoxParser.StringContext;
import de.hpi.swa.lox.parser.LoxParser.TermContext;
import de.hpi.swa.lox.parser.LoxParser.TrueContext;
import de.hpi.swa.lox.parser.LoxParser.UnaryContext;
import de.hpi.swa.lox.parser.LoxParser.VarDeclContext;
import de.hpi.swa.lox.parser.LoxParser.VariableExprContext;
import de.hpi.swa.lox.parser.LoxParser.WhileStmtContext;
import de.hpi.swa.lox.runtime.object.Nil;

/**
 * Lox AST visitor that parses to Bytecode DSL bytecode.
 */
public final class LoxBytecodeCompiler extends LoxBaseVisitor<Void> {

    protected final LoxLanguage language;
    protected final Source source;
    private LexicalScope curScope = null;
    private BytecodeLabel breakLabel;
    private BytecodeLabel continueLabel;

    private final LoxBytecodeRootNodeGen.Builder b;

    public static RootCallTarget parseLox(LoxLanguage language, Source source) {
        BytecodeParser<LoxBytecodeRootNodeGen.Builder> bytecodeParser = (b) -> {
            LoxBytecodeCompiler visitor = new LoxBytecodeCompiler(language, source, b);
            b.beginSource(source);
            LoxLexer lexer = new LoxLexer(CharStreams.fromString(source.getCharacters().toString()));
            LoxParser loxParser = new LoxParser(new CommonTokenStream(lexer));

            // Detect syntax errors
            lexer.removeErrorListeners();
            loxParser.removeErrorListeners();
            BailoutErrorListener listener = new BailoutErrorListener(source);
            lexer.addErrorListener(listener);
            loxParser.addErrorListener(listener);

            loxParser.program().accept(visitor);
            b.endSource();
        };
        var config = LoxBytecodeRootNodeGen.newConfigBuilder().build();
        var nodes = LoxBytecodeRootNodeGen.create(language, config, bytecodeParser).getNodes();
        nodes.getFirst().dump();
        return nodes.get(0).getCallTarget();
    }

    private static final class BailoutErrorListener extends BaseErrorListener {
        private final Source source;

        BailoutErrorListener(Source source) {
            this.source = source;
        }

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
                String msg, RecognitionException e) {
            throwParseError(source, line, charPositionInLine, (Token) offendingSymbol, msg);
        }
    }

    private static void throwParseError(Source source, int line, int charPositionInLine, Token token, String message) {
        int col = charPositionInLine + 1;
        String location = "-- line " + line + " col " + col + ": ";
        int length = token == null ? 1 : Math.max(token.getStopIndex() - token.getStartIndex(), 0);
        throw new LoxParseError(source, line, col, length,
                String.format("Error(s) parsing script:%n" + location + message));
    }

    private void beginAttribution(ParseTree tree) {
        beginAttribution(getStartIndex(tree), getEndIndex(tree));
    }

    private static int getEndIndex(ParseTree tree) {
        switch (tree) {
            case ParserRuleContext ctx -> {
                return ctx
                        .getStop().getStopIndex();
            }
            case TerminalNode node -> {
                return node
                        .getSymbol().getStopIndex();
            }
            default -> throw new AssertionError(
                    "unknown tree type: "
                            + tree);
        }
    }

    private static int getStartIndex(ParseTree tree) {
        switch (tree) {
            case ParserRuleContext ctx -> {
                return ctx
                        .getStart().getStartIndex();
            }
            case TerminalNode node -> {
                return node
                        .getSymbol().getStartIndex();
            }
            default -> throw new AssertionError(
                    "unknown tree type: "
                            + tree);
        }
    }

    private void beginAttribution(int start, int end) {
        int length = end
                - start
                +
                1;
        assert length >= 0;
        b
                .beginSourceSection(start, length);
    }

    private void endAttribution() {
        b.endSourceSection();
    }

    private LoxBytecodeCompiler(LoxLanguage language, Source source, LoxBytecodeRootNodeGen.Builder builder) {
        this.language = language;
        this.source = source;
        this.b = builder;
        this.curScope = new LexicalScope();
    }

    @Override
    public Void visitProgram(ProgramContext ctx) {
        b.beginRoot();
        var result = super.visitProgram(ctx);
        b.beginReturn();
        b.emitLoadConstant(0);
        b.endReturn();
        b.endRoot();
        return result;
    }

    @Override
    public Void visitTrue(TrueContext ctx) {
        b.emitLoadConstant(true);
        return super.visitTrue(ctx);
    }

    @Override
    public Void visitFalse(FalseContext ctx) {
        b.emitLoadConstant(false);
        return super.visitFalse(ctx);
    }

    @Override
    public Void visitNil(NilContext ctx) {
        b.emitLoadConstant(Nil.INSTANCE);
        return super.visitNil(ctx);
    }

    @Override
    public Void visitPrintStmt(PrintStmtContext ctx) {
        b.beginLoxPrint();
        var result = super.visitPrintStmt(ctx);
        b.endLoxPrint();
        return result;
    }

    @Override
    public Void visitString(StringContext ctx) {
        List<Object> parts = new ArrayList<>();

        // Remove surrounding quotes
        String stringContent = ctx.getText().substring(1, ctx.getText().length() - 1);

        // Regex to match ${...} expressions
        Pattern pattern = Pattern.compile("(\\$\\{(.*?)\\})");
        Matcher matcher = pattern.matcher(stringContent);

        int lastIndex = 0;
        while (matcher.find()) {
            // Add the text before the expression as a string constant
            if (matcher.start() > lastIndex) {
                String literalPart = stringContent.substring(lastIndex, matcher.start());
                literalPart = "\"" + literalPart + "\"";
                LoxLexer lexer = new LoxLexer(CharStreams.fromString(literalPart));
                LoxParser parser = new LoxParser(new CommonTokenStream(lexer));
                LoxParser.ExpressionContext expr = parser.expression();
                parts.add(expr);
            }

            // Convert the expression inside ${} to an AST node (ExpressionContext)
            String expressionStr = matcher.group(2).trim();
            LoxLexer lexer = new LoxLexer(CharStreams.fromString(expressionStr));
            LoxParser parser = new LoxParser(new CommonTokenStream(lexer));
            LoxParser.ExpressionContext expr = parser.expression();
            parts.add(expr); // Store as AST node instead of raw text

            lastIndex = matcher.end();
        }

        // Add remaining text after last match, wrapped in quotes
        if (lastIndex < stringContent.length()) {
            String literalPart = stringContent.substring(lastIndex);
            parts.add("\"" + literalPart + "\"");
        }

        // Determine how many concatenations are needed
        int concatCount = parts.size() - 1;

        // Begin additions BEFORE emitting constants
        for (int i = 0; i < concatCount; i++) {
            b.beginLoxAdd();
        }

        // Emit all parts
        for (int i = 0; i < parts.size(); i++) {
            Object part = parts.get(i);
            switch (part) {
                case LoxParser.ExpressionContext expressionContext -> visitExpression(expressionContext);
                case String string -> {
                    // Convert to TruffleString before emitting
                    var ts = TruffleString.fromJavaStringUncached(string.substring(1, ctx.getText().length() - 1),
                            TruffleString.Encoding.UTF_8);
                    b.emitLoadConstant(ts);
                }
                default -> {
                }
            }

            if (i > 0) {
                b.endLoxAdd();
            }
        }

        return super.visitString(ctx);
    }

    @Override
    public Void visitNumber(NumberContext ctx) {
        String literal = ctx.getText();
        if (literal.contains(".")) {
            double number = Double.parseDouble(literal);
            b.emitLoadConstant(number);
        } else {
            try {
                long number = Long.parseLong(literal);
                b.emitLoadConstant(number);
            } catch (NumberFormatException e) {
                BigInteger bigInteger = new BigInteger(literal);
                b.emitLoadConstant(bigInteger);
            }
        }
        return super.visitNumber(ctx);
    }

    @Override
    public Void visitTerm(TermContext ctx) {
        if (ctx.getChildCount() == 1) {
            return super.visitTerm(ctx);
        }

        for (int i = ctx.getChildCount() - 2; i >= 0; i -= 2) {
            var operation = ctx.getChild(i).getText();
            switch (operation) {
                case "+" -> b.beginLoxAdd();
                case "-" -> b.beginLoxSub();
            }
        }

        visitFactor(ctx.factor(0));

        for (int i = 1; i < ctx.getChildCount() - 1; i += 2) {
            visitFactor(ctx.factor((i + 1) / 2));
            var operation = ctx.getChild(i).getText();
            switch (operation) {
                case "+" -> b.endLoxAdd();
                case "-" -> b.endLoxSub();
            }
        }

        return null;
    }

    @Override
    public Void visitFactor(FactorContext ctx) {
        if (ctx.getChildCount() == 1) {
            return super.visitFactor(ctx);
        }

        for (int i = ctx.getChildCount() - 2; i >= 0; i -= 2) {
            var operation = ctx.getChild(i).getText();
            switch (operation) {
                case "*" -> b.beginLoxMul();
                case "/" -> b.beginLoxDiv();
                case "%" -> b.beginLoxMod();
            }
        }

        visitUnary(ctx.unary(0));

        for (int i = 1; i < ctx.getChildCount() - 1; i += 2) {
            visitUnary(ctx.unary((i + 1) / 2));
            var operation = ctx.getChild(i).getText();
            switch (operation) {
                case "*" -> b.endLoxMul();
                case "/" -> b.endLoxDiv();
                case "%" -> b.endLoxMod();
            }
        }

        return null;
    }

    @Override
    public Void visitComparison(ComparisonContext ctx) {
        if (ctx.getChildCount() == 1) {
            return super.visitComparison(ctx);
        }

        List<LoxParser.TermContext> terms = ctx.term();
        int countOp = terms.size() - 1;

        if (countOp == 0) {
            visitTerm(terms.get(0));
            return null;
        }

        // Maintain a stack of AND blocks
        for (int i = 0; i < countOp - 1; i++) {
            b.beginLoxAnd();
        }

        for (int i = 0; i < countOp; i++) {
            var operation = ctx.getChild(i * 2 + 1).getText();
            switch (operation) {
                case ">" -> b.beginLoxGreaterThan();
                case ">=" -> b.beginLoxGreaterEqual();
                case "<" -> b.beginLoxLessThan();
                case "<=" -> b.beginLoxLessEqual();
            }

            // Emit the left-hand side operand
            visitTerm(terms.get(i));
            // Emit the right-hand side operand
            visitTerm(terms.get(i + 1));

            switch (operation) {
                case ">" -> b.endLoxGreaterThan();
                case ">=" -> b.endLoxGreaterEqual();
                case "<" -> b.endLoxLessThan();
                case "<=" -> b.endLoxLessEqual();
            }

            if (i >= 1) {
                b.endLoxAnd();
            }
        }

        return null;
    }

    @Override
    public Void visitEquality(EqualityContext ctx) {
        List<String> operations = new ArrayList<>();

        for (int i = 1; i < ctx.getChildCount(); i += 2) {
            var operation = ctx.getChild(i).getText();
            operations.add(operation);
            switch (operation) {
                case "==" -> b.beginLoxEqual();
                case "!=" -> b.beginLoxNotEqual();
            }
        }

        visitComparison(ctx.comparison(0));

        for (int i = 1; i < ctx.getChildCount(); i += 2) {
            visitComparison(ctx.comparison(i));
            String operation = operations.remove(operations.size() - 1);
            switch (operation) {
                case "==" -> b.endLoxEqual();
                case "!=" -> b.endLoxNotEqual();
            }
        }

        return null;
    }

    @Override
    public Void visitUnary(UnaryContext ctx) {
        if (ctx.getChildCount() == 2) {
            String operator = ctx.getChild(0).getText();

            switch (operator) {
                case "!" -> b.beginLoxNot();
                case "-" -> b.beginLoxNeg();
            }

            visitUnary(ctx.unary());

            switch (operator) {
                case "!" -> b.endLoxNot();
                case "-" -> b.endLoxNeg();
            }
        } else {
            visitPrimary(ctx.primary());
        }
        return null;
    }

    @Override
    public Void visitLogic_or(Logic_orContext ctx) {
        if (ctx.getChildCount() == 1) {
            return super.visitLogic_or(ctx);
        }
        for (int i = 1; i < ctx.getChildCount(); i += 2) {
            var operation = ctx.getChild(i).getText();
            if ("or".equals(operation)) {
                b.beginLoxOr();
            }
        }

        visitLogic_and(ctx.logic_and(0));

        for (int i = 1; i < ctx.logic_and().size(); i++) {
            visitLogic_and(ctx.logic_and(i));
            var operation = ctx.getChild(i * 2 - 1).getText();
            if ("or".equals(operation)) {
                b.endLoxOr();
            }
        }

        return null;

    }

    @Override
    public Void visitLogic_and(Logic_andContext ctx) {
        if (ctx.getChildCount() == 1) {
            return super.visitLogic_and(ctx);
        }
        for (int i = 1; i < ctx.getChildCount(); i += 2) {
            var operation = ctx.getChild(i).getText();
            if ("and".equals(operation)) {
                b.beginLoxAnd();
            }
        }

        visitEquality(ctx.equality(0));

        for (int i = 1; i < ctx.equality().size(); i++) {
            visitEquality(ctx.equality(i));
            var operation = ctx.getChild(i * 2 - 1).getText();
            if ("and".equals(operation)) {
                b.endLoxAnd();
            }
        }

        return null;
    }

    @Override
    public Void visitStatement(StatementContext ctx) {
        ParseTree tree = ctx;
        beginAttribution(tree);
        super.visitStatement(ctx);
        endAttribution();
        return null;
    }

    @Override
    public Void visitVarDecl(VarDeclContext ctx) {
        var localName = ctx.IDENTIFIER().getText();
        curScope.define(localName, ctx);
        if (ctx.expression() != null) {
            curScope.beginStore(localName);
            visit(ctx.expression());
            curScope.endStore();
        }
        return null;
    }

    @Override
    public Void visitVariableExpr(VariableExprContext ctx) {
        curScope.load(ctx.getText());
        return null;
    }

    @Override
    public Void visitAssignment(AssignmentContext ctx) {
        final boolean isAssignment = ctx.IDENTIFIER() != null;
        String text = null;
        if (isAssignment) {
            b.beginBlock(); // for grouping the the storing an the loading together
            text = ctx.IDENTIFIER().getText();
            curScope.beginStore(text);
        }
        super.visitAssignment(ctx);
        if (isAssignment) {
            curScope.endStore();
            curScope.load(text); // for the value of the assignment
            b.endBlock();
        }
        return null;
    }

    @Override
    public Void visitBlock(BlockContext ctx) {
        b.beginBlock();
        curScope = new LexicalScope(curScope);
        super.visitBlock(ctx);
        curScope = curScope.parent;
        b.endBlock();
        return null;
    }

    @Override
    public Void visitIfStmt(IfStmtContext ctx) {
        if (ctx.alt == null) {
            b.beginIfThen();
            beginAttribution(ctx.condition);
            b.beginLoxIsTruthy();
            visit(ctx.condition);
            b.endLoxIsTruthy();
            endAttribution();
            visit(ctx.then);
            b.endIfThen();
        } else {
            b.beginIfThenElse();
            beginAttribution(ctx.condition);
            b.beginLoxIsTruthy();
            visit(ctx.condition);
            b.endLoxIsTruthy();
            endAttribution();
            visit(ctx.then);
            visit(ctx.alt);
            b.endIfThenElse();
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(WhileStmtContext ctx) {
        BytecodeLabel oldBreak = breakLabel;
        BytecodeLabel oldContinue = continueLabel;
        b.beginBlock();
        breakLabel = b.createLabel();
        continueLabel = b.createLabel();
        b.emitLabel(continueLabel);
        b.beginWhile();

        beginAttribution(ctx.condition);
        b.beginLoxIsTruthy();
        visit(ctx.condition);
        b.endLoxIsTruthy();
        endAttribution();

        visit(ctx.body);

        b.endWhile();
        b.emitLabel(breakLabel);

        b.endBlock();
        breakLabel = oldBreak;
        continueLabel = oldContinue;

        return null;
    }

    @Override
    public Void visitForStmt(ForStmtContext ctx) {
        BytecodeLabel oldBreak = breakLabel;
        BytecodeLabel oldContinue = continueLabel;

        curScope = new LexicalScope(curScope);

        b.beginBlock();
        ParserRuleContext init = ctx.varDecl();
        if (init == null) {
            init = ctx.exprStmt();
        }
        if (init != null) {
            visit(init);
        }

        breakLabel = b.createLabel();
        continueLabel = b.createLabel();
        b.emitLabel(continueLabel);

        b.beginWhile();
        beginAttribution(ctx.condition);
        b.beginLoxIsTruthy();
        visit(ctx.condition);
        b.endLoxIsTruthy();
        endAttribution();
        b.beginBlock();
        visit(ctx.body);
        visit(ctx.increment);
        b.endBlock();
        b.endWhile();

        curScope = curScope.parent;
        b.emitLabel(breakLabel);
        b.endBlock();
        breakLabel = oldBreak;
        continueLabel = oldContinue;

        return null;
    }

    @Override
    public Void visitArray(ArrayContext ctx) {
        List<LoxParser.ExpressionContext> elements = ctx.expression();

        if (elements.isEmpty()) {
            b.emitLoxNewArray();
        } else {
            b.beginLoxArrayLiterals();
            for (ExpressionContext expr : elements) {
                visit(expr);
            }
            b.endLoxArrayLiterals();
        }

        return null;
    }

    @Override
    public Void visitArrayExpr(ArrayExprContext ctx) {
        b.beginLoxReadArray();
        visit(ctx.left);
        visit(ctx.index);
        b.endLoxReadArray();
        return null;
    }

    @Override
    public Void visitArrAssignment(ArrAssignmentContext ctx) {
        if (ctx.other != null) {
            return visit(ctx.other);
        }
        b.beginLoxWriteArray();
        visit(ctx.left);
        visit(ctx.index);
        visit(ctx.right);
        b.endLoxWriteArray();
        return null;
    }

    @Override
    public Void visitBreakStmt(BreakStmtContext ctx) {
        b.emitBranch(breakLabel);
        return null;
    }

    @Override
    public Void visitContinueStmt(ContinueStmtContext ctx) {
        b.emitBranch(continueLabel);
        return null;
    }

    private class LexicalScope {
        final LexicalScope parent;
        final Map<String, BytecodeLocal> locals;
        private Stack<Object> currentStore = new Stack<>();

        LexicalScope(LexicalScope parent) {
            this.parent = parent;
            locals = new HashMap<>();
        }

        LexicalScope() {
            this(null);
        }

        public void define(String localName, ParseTree ctx) {
            if (parent != null) {
                if (locals.get(localName) != null) {
                    throw LoxParseError.build(source, ctx, "Variable " + localName + " already defined");
                }
                var local = b.createLocal(localName, null);
                locals.put(localName, local);
            } else {
                b.emitLoxDefineGlobalVariable(localName);
            }
        }

        private BytecodeLocal lookupName(String name) {
            var scope = this;
            BytecodeLocal variable;
            do {
                variable = scope.locals.get(name);
                scope = scope.parent;
            } while (variable == null && scope != null);
            return variable;
        }

        public void beginStore(String name) {
            var variable = lookupName(name);
            this.currentStore.push(variable);
            if (variable != null) {
                b.beginStoreLocal(variable);
            } else {
                b.beginLoxWriteGlobalVariable(name);
            }
        }

        public void endStore() {
            var currentStore = this.currentStore.pop();
            if (currentStore != null) {
                b.endStoreLocal();
            } else {
                b.endLoxWriteGlobalVariable();
            }
        }

        public void load(String text) {
            var variable = lookupName(text);
            if (variable != null) {
                b.beginBlock();
                b.emitLoxCheckLocalDefined(variable);
                b.emitLoadLocal(variable);
                b.endBlock();
            } else {
                b.emitLoxReadGlobalVariable(text);
            }
        }
    }

}
