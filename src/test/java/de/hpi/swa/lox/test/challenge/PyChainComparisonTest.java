package de.hpi.swa.lox.test.challenge;

import org.junit.Test;

import de.hpi.swa.lox.test.AbstractLoxTest;

public class PyChainComparisonTest extends AbstractLoxTest {

    @Test
    public void testSimpleChain() {
        runAndExpect("testSimpleChain", "print 6 > 4 > 3;", "true\n");
    }

    @Test
    public void testLongerChain() {
        runAndExpect("testLongerChain", "print 6 > 4 > 3 > 2;", "true\n");
    }

    @Test
    public void testMixedChain() {
        runAndExpect("testMixedChain", "print 10 > 5 < 8;", "true\n");
    }

    @Test
    public void testFalseChain() {
        runAndExpect("testFalseChain", "print 10 > 5 > 8;", "false\n");
    }

    @Test
    public void testEdgeCase() {
        runAndExpect("testEdgeCase", "print 3 >= 3 > 2;", "true\n");
    }
}
