package de.hpi.swa.lox.test.challenge;

import org.junit.Test;

import de.hpi.swa.lox.test.AbstractLoxTest;

public class ArrayLiteralsTest extends AbstractLoxTest {
    @Test
    public void testArrayWithLiterals() {
        runAndExpect("array with literals", "var a = [1, \"hello\"]; print a;", "[1, \"hello\"]\n");
    }

    @Test
    public void testGetArrayElements() {
        runAndExpect("get first element", "var a = [1, \"hello\"]; print a[0];", "1\n");
        runAndExpect("get second element", "print a[1];", "hello\n");
    }

    @Test
    public void testSetArrayElement() {
        runAndExpect("set element", "var a = [1, \"hello\"]; a[0] = 42; print a;", "[42, \"hello\"]\n");
    }

}
