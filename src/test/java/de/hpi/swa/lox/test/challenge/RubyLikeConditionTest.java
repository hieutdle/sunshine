package de.hpi.swa.lox.test.challenge;

import org.junit.Test;

import de.hpi.swa.lox.test.AbstractLoxTest;

public class RubyLikeConditionTest extends AbstractLoxTest {
    @Test
    public void testRubyLikeConditionIf() {
        runAndExpect("Ruby-like if statement",
                "var a = 5; print a if (a > 0);",
                "5\n");

        runAndExpect("Ruby-like if with false condition",
                "a = -3;print a if (a > 0) ;",
                ""); // No output expected
    }

    @Test
    public void testRubyLikeConditionUnless() {
        runAndExpect("Ruby-like unless statement",
                "var a = 5; print a unless (a < 0);",
                "5\n");

        runAndExpect("Ruby-like unless with false condition",
                "a = -3; print a unless (a < 0);",
                ""); // No output expected
    }
}
