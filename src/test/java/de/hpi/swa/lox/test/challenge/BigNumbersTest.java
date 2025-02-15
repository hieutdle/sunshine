package de.hpi.swa.lox.test.challenge;

import org.junit.Test;

import de.hpi.swa.lox.test.AbstractLoxTest;

public class BigNumbersTest extends AbstractLoxTest {

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

    }
}
