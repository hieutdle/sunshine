package de.hpi.swa.lox.test.challenge;

import org.junit.Test;

import de.hpi.swa.lox.test.AbstractLoxTest;

public class FormatStringTest extends AbstractLoxTest {
    @Test
    public void printInterpolatedString() {
        runAndExpect("printTestOutput", "print \"hello ${3 + 4}\";", "hello 7\n");
    }

    // @Test
    // public void printMultipleInterpolations() {
    //     runAndExpect("printTestOutput", "print \"sum: ${2 + 3}, product: ${4 * 5}\";", "sum: 5, product: 20\n");
    // }

    // @Test
    // public void printNestedInterpolations() {
    //     runAndExpect("printTestOutput", "print \"nested ${\"value: ${1 + 1}\"}\";", "nested value: 2\n");
    // }

    // @Test
    // public void printInterpolationWithVariables() {
    //     runAndExpect("printTestOutput", "var x = 10; print \"x is ${x}\";", "x is 10\n");
    // }

    // @Test
    // public void printInterpolationWithFunctionCalls() {
    //     runAndExpect("printTestOutput", "fun add(a, b) { return a + b; } print \"result: ${add(2, 3)}\";",
    //             "result: 5\n");
    // }
}
