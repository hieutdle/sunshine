package de.hpi.swa.lox.test.basic;

import org.junit.Test;

import de.hpi.swa.lox.test.AbstractLoxTest;

public class ClosureTest extends AbstractLoxTest {
    @Test
    public void testInnerFunction() {
        runAndExpect(
                "inner function",
                "fun outer() { "
                        + " fun inner() { "
                        + "   return 1; "
                        + " }"
                        + " return inner();"
                        + "}"
                        + "print outer();",
                "1\n");
    }

    @Test
    public void testInnerFunctionReadsOuterVariable() {
        runAndExpect(
                "inner function uses outer variable",
                "fun outer() { "
                        + " var a = 1; "
                        + " fun inner() { "
                        + "   return a; "
                        + " }"
                        + " return inner();"
                        + "}"
                        + "print outer();",
                "1\n");
    }

    @Test
    public void testInnerFunctionStoreOuterVariable() {
        runAndExpect(
                "inner function uses outer variable",
                "fun outer() { "
                        + " var a = 1; "
                        + " fun inner() { "
                        + "   a = 2; "
                        + "   return a; "
                        + " }"
                        + " return inner();"
                        + "}"
                        + "print outer();",
                "2\n");
    }

    @Test
    public void testFunctionReturnsFunction() {
        runAndExpect(
                "function returns function",
                "fun outer() {"
                        + " fun inner() {"
                        + "   return 1; "
                        + " }"
                        + " return inner; "
                        + "}"
                        + "print outer()();",
                "1\n");
    }
}
