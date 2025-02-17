package de.hpi.swa.lox.test.challenge;

import org.junit.Test;

import de.hpi.swa.lox.test.AbstractLoxTest;

public class ArrayIteratorsTest extends AbstractLoxTest {
    @Test
    public void testArrayForInIteration() {
        runAndExpect("testArrayForInIteration", """
                var a = [3,4];
                for (var i in a) {
                     a[i] = a[i] * 2;
                }
                print(a[0] + a[1]);
                """, "14\n");
    }

}
