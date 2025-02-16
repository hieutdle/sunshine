package de.hpi.swa.lox.parser;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
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
import com.oracle.truffle.api.bytecode.BytecodeParser;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.strings.TruffleString;

import de.hpi.swa.lox.LoxLanguage;
import de.hpi.swa.lox.bytecode.LoxBytecodeRootNodeGen;
import de.hpi.swa.lox.error.LoxParseError;
import de.hpi.swa.lox.parser.LoxParser.FalseContext;
import de.hpi.swa.lox.parser.LoxParser.NilContext;
import de.hpi.swa.lox.parser.LoxParser.PrintStmtContext;
import de.hpi.swa.lox.parser.LoxParser.ProgramContext;
import de.hpi.swa.lox.parser.LoxParser.StatementContext;
import de.hpi.swa.lox.parser.LoxParser.StringContext;
import de.hpi.swa.lox.parser.LoxParser.TrueContext;
import de.hpi.swa.lox.parser.LoxParser.UnaryContext;
import de.hpi.swa.lox.runtime.object.Nil;

/**
 * Lox AST visitor that parses to Bytecode DSL bytecode.
 */
public final class LoxBytecodeCompiler extends LoxBaseVisitor<Void> {

    protected final LoxLanguage language;
    protected final Source source;

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
    public Void visitNumber(LoxParser.NumberContext ctx) {
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
    public Void visitTerm(LoxParser.TermContext ctx) {
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
    public Void visitFactor(LoxParser.FactorContext ctx) {
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
    public Void visitComparison(LoxParser.ComparisonContext ctx) {
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
    public Void visitEquality(LoxParser.EqualityContext ctx) {
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
    public Void visitLogic_or(LoxParser.Logic_orContext ctx) {
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
    public Void visitLogic_and(LoxParser.Logic_andContext ctx) {
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

}
