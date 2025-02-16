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
}