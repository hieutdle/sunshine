package de.hpi.swa.lox.test.basic;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Test;

import de.hpi.swa.lox.test.AbstractLoxTest;

public class StatementsTest extends AbstractLoxTest {
    @Test
    public void testExtressionStatement() {
        run("3 + 4;");
        assertThat(errContent.toString(), is(""));
    }

    @Test
    public void testBlockStatement() {
        run("{3 + 4;}");
        assertThat(errContent.toString(), is(""));
    }

    @Test
    public void testTwoStatement() {
        runAndExpect("two statements", "print 1; print 2;", "1\n2\n");
    }

    @Test
    public void testTwoStatementsWithNewline() {
        runAndExpect("two statements", "print 1;\n print 2;", "1\n2\n");
    }

    @Test
    public void testWriteAndReadVariable() {
        runAndExpect("read write and read variable", "var a = 3; print a;", "3\n");
    }

    @Test
    public void testBlockWriteAndReadVariable() {
        runAndExpect("read write and read variable", "{var a = 4; print a;}", "4\n");
    }

    @Test
    public void testShadowedWriteAndReadVariable() {
        runAndExpect("read write and read variable", "var a = 3; print a; {var a = 4; print a;} print a;", "3\n4\n3\n");
    }

    @Test
    public void testReadUndefinedLocalVariable() {
        runAndExpectError("read undefined variable", "{var a; print a;}", "not defined");
    }

    @Test
    public void testReadUndeclaredLocalVariable() {
        runAndExpectError("read undefined variable", "{print a;}", "not declared");
    }

    @Test
    public void testAssignmentReturnsValue() {
        runAndExpect("assignment returns value", "var a = 1; print a = 2;", "2\n");
    }
}