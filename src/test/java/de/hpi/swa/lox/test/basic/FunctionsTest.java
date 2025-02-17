package de.hpi.swa.lox.test.basic;

import org.junit.Test;

import de.hpi.swa.lox.test.AbstractLoxTest;

public class FunctionsTest extends AbstractLoxTest {
    @Test
    public void testFunctionDeclaration() {
        runAndExpect("function declaration", "print 2;fun f() { print 1; }; print 3;", "2\n3\n");
    }

    @Test
    public void testFunctionCall() {
        runAndExpect("function declaration", "fun f() { print 1; } f();f();", "1\n1\n" + //
                "");
    }

    @Test
    public void testFunctionAsValues() {
        runAndExpect("function expression", "fun x() { print 1; };  var f = x; f();", "1\n");
    }

    @Test
    public void testFunctionDeclarationWithParameter() {
        runAndExpect("function declaration with parameter", "fun f(a) { print a; } f(1);", "1\n");
    }

    @Test
    public void testFunctionReturn() {
        runAndExpect("function return", "fun f() { return 1; } print f();", "1\n");
    }
}