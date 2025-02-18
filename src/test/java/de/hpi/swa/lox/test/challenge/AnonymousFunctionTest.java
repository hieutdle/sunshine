package de.hpi.swa.lox.test.challenge;

import org.junit.Test;

import de.hpi.swa.lox.test.AbstractLoxTest;

public class AnonymousFunctionTest extends AbstractLoxTest {

    @Test
    public void testAnonymousFunctionDeclaration() {
        runAndExpect("anonymous function declaration",
                "var f = () => 42; print f();",
                "42\n");
    }

    @Test
    public void testAnonymousFunctionReturnValue() {
        runAndExpect("anonymous function return value",
                "var f = () => { return 99; }; print f();",
                "99\n");
    }

    @Test
    public void testAnonymousFunctionWithoutBraces() {
        runAndExpect("anonymous function without braces",
                "var f = x => x * 2; print f(5);",
                "10\n");
    }

    @Test
    public void testAnonymousFunctionWithMultipleParameters() {
        runAndExpect("anonymous function with multiple parameters",
                "var add = (a, b) => a + b; print add(3, 4);",
                "7\n");
    }

    @Test
    public void testAnonymousFunctionInsideFunction() {
        runAndExpect("anonymous function inside function",
                "fun outer() { return () => { return 88; }; } " +
                        "var inner = outer(); print inner();",
                "88\n");
    }
}
