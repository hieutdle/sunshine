package de.hpi.swa.lox.test.basic;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

import de.hpi.swa.lox.test.AbstractLoxTest;

public class ClockTest extends AbstractLoxTest {
       @Test
    public void testClock() {
        run("print clock();");
        String output = this.outContent.toString().trim();
        assertTrue("Expected a number but got: " + output, output.matches("[0-9.E]+"));
    }

}
