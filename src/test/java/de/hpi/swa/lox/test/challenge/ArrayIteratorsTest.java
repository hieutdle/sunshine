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

    @Test
    public void testArrayForOfIteration() {
        runAndExpect("testArrayForOfIteration", """
                var a = [];
                a[0] = 3;
                a[1] = 4;
                var sum = 0;
                for (var ea of a) {
                    sum = sum + ea;
                }
                print(sum);
                """, "7\n");
    }
}
