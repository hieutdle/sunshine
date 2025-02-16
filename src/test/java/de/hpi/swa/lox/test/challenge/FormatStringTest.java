package de.hpi.swa.lox.test.challenge;

import org.junit.Test;

import de.hpi.swa.lox.test.AbstractLoxTest;

public class FormatStringTest extends AbstractLoxTest {
    @Test
    public void printInterpolatedString() {
        runAndExpect("printTestOutput", "print \"hello ${3 + 4}\";", "hello 7\n");
    }

    // @Test
    // public void testSimpleFormatString() {
    //     runAndExpect("simpleFormatString", "print \"hello ${3 + 4}\";", "hello 7\n");
    // }

    // @Test
    // public void testMultipleFormatString() {
    //     runAndExpect("multipleFormatString", "print \"sum: ${2 + 3}, product: ${4 * 5}\";", "sum: 5, product: 20\n");
    // }

    // @Test
    // public void testNestedFormatString() {
    //     runAndExpect("nestedFormatString", "print \"sum: ${2 + 3}, product: ${4 * 5}\";", "sum: 5, product: 20\n");
    // }

    // @Test
    // public void testFormatStringWithVariables() {
    //     runAndExpect("formatStringWithVariables", "var x = 10; print \"x is ${x}\";", "x is 10\n");
    // }

    // @Test
    // public void testFormatStringWithFunctionCalls() {
    //     runAndExpect("formatStringWithFunctionCalls", "fun add(a, b) { return a + b; } print \"result: ${add(2, 3)}\";",
    //             "result: 5\n");
    // }
}
