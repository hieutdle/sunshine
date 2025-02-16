package de.hpi.swa.lox.test.challenge;

import org.junit.Test;

import de.hpi.swa.lox.test.AbstractLoxTest;

public class ModuloTest extends AbstractLoxTest {

    @Test
    public void testModulo() {
        runAndExpect("testModulo", "print 10 % 3;", "1\n");
        runAndExpect("testModulo", "print 10 % 2;", "0\n");
        runAndExpect("testModulo", "print -10 % 3;", "-1\n");
        runAndExpect("testModulo", "print 10 % -3;", "1\n");
        runAndExpect("testModulo", "print -10 % -3;", "-1\n");
    }

    @Test
    public void testBigNumbersModulo() {
        runAndExpect("testBigNumbersModuloMixed", "print 9223372036854775808 % 3;", "2\n");
        runAndExpect("testBigNumbersModuloBig", "print 18446744073709551616 % 9223372036854775808;", "0\n");
    }

}
