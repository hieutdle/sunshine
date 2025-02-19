package de.hpi.swa.lox.test.challenge;

import org.junit.Test;

import de.hpi.swa.lox.test.AbstractLoxTest;

public class StaticFunctionTest extends AbstractLoxTest {
    @Test
    public void testStaticAndInstanceMethods() {
        runAndExpect("static and instance method mix",
                "class Math { static square(n) { return n * n; } power(n) { return n * n; } } " +
                        "var m = Math(); print Math.square(3); print m.power(3);",
                "9\n9\n");
    }

    @Test
    public void testCallInstanceMethodOnClass() {
        runAndExpectError("calling instance method on class",
                "class Math { power(n) { return n * n; } } print Math.power(3);",
                "Undefined (static) property 'power' for class Math");
    }

}
