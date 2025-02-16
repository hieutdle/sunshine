package de.hpi.swa.lox.test.basic;

import org.junit.Test;

import de.hpi.swa.lox.test.AbstractLoxTest;

public class DatatypesTest extends AbstractLoxTest {
    @Test
    public void testBoolean() {
        runAndExpect("testBoolean", "print true;", "true\n");
        runAndExpect("testBoolean", "print false;", "false\n");
    }

    @Test
    public void testString() {
        runAndExpect("testString", "print \"Hello, World!\";", "Hello, World!\n");
    }

    @Test
    public void testNumber() {
        runAndExpect("testNumber", "print 42;", "42\n");
        runAndExpect("testNumber", "print 2.5;", "2.5\n");
    }

    @Test
    public void testNil() {
        runAndExpect("testNil", "print nil;", "nil\n");
    }

    @Test
    public void testBigNumbers() {
        // Printing Big Numbers
        runAndExpect("testPrintBigNumbers", "print 9223372036854775808;", "9223372036854775808\n");

        // Addition Tests
        runAndExpect("testBigNumbersAddTwoLong", "print 9223372036854775807+9223372036854775807;",
                "18446744073709551614\n");
        runAndExpect("testBigNumbersAddTwoBigNumbers", "print 9223372036854775808+9223372036854775808;",
                "18446744073709551616\n");

        // Multiplication Tests
        runAndExpect("testBigNumbersMulTwoLong", "print 9223372036854775807*2;", "18446744073709551614\n");
        runAndExpect("testBigNumbersMulTwoBigNumbers", "print 9223372036854775807*9223372036854775807;",
                "85070591730234615847396907784232501249\n");

        // Subtraction Tests
        runAndExpect("testBigNumbersSubMixed", "print -9223372036854775808-9223372036854775807;",
                "-18446744073709551615\n");

        // Division Tests
        runAndExpect("testBigNumbersDivMixed", "print 9223372036854775808/2;", "4611686018427387904\n");

        // Mixed Operations
        runAndExpect("testBigNumbersMixedOperations", "print (9223372036854775807 + 1) * 2;", "18446744073709551616\n");

        // Comparisons
        runAndExpect("testBigNumbersEquality", "print 9223372036854775808 == 9223372036854775808;", "true\n");
        runAndExpect("testBigNumbersInequality", "print 9223372036854775807 != 9223372036854775808;", "true\n");
        runAndExpect("testBigNumbersGreaterThan", "print 9223372036854775808 > 9223372036854775807;", "true\n");
        runAndExpect("testBigNumbersLessThan", "print 9223372036854775807 < 9223372036854775808;", "true\n");

        // Negative Big Number
        runAndExpect("testNegativeBigNumbers", "print -9223372036854775809;", "-9223372036854775809\n");

        // Modulo
        runAndExpect("testBigNumbersModuloMixed", "print 9223372036854775808 % 3;", "2\n");
        runAndExpect("testBigNumbersModuloBig", "print 18446744073709551616 % 9223372036854775808;", "0\n");
    }
}