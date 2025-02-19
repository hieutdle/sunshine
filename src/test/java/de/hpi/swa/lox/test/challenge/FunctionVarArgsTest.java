package de.hpi.swa.lox.test.challenge;

import org.junit.Test;
import de.hpi.swa.lox.test.AbstractLoxTest;

public class FunctionVarArgsTest extends AbstractLoxTest {

    @Test
    public void testVarArgsFunction() {
        runAndExpect("varargs function", """
                fun f(a, ...args) {
                    print a;
                    print args;
                }
                f(1, 2, 3, 4);
                """, "1\n[2, 3, 4]\n");
    }

    @Test
    public void testVarArgsEmpty() {
        runAndExpect("varargs empty", """
                fun f(a, ...args) {
                    print a;
                    print args;
                }
                f(1);
                """, "1\n[]\n");
    }

    @Test
    public void testVarArgsSingle() {
        runAndExpect("varargs single", """
                fun f(a, ...args) {
                    print a;
                    print args;
                }
                f(1, 2);
                """, "1\n[2]\n");
    }

    @Test
    public void testVarArgsInLoop() {
        runAndExpect("varargs loop", """
                fun f(...args) {
                    for (var i in args) {
                        print args[i];
                    }
                }
                f(10, 20, 30);
                """, "10\n20\n30\n");
    }
}
