package de.hpi.swa.lox.test.basic;

import org.junit.Test;

import de.hpi.swa.lox.test.AbstractLoxTest;

public class ConTrolFlowTest extends AbstractLoxTest {
    @Test
    public void testIfCondition() {
        runAndExpect("if true", "if(true) print 1;", "1\n");
        runAndExpect("if true", "if(true) { print 1;}", "1\n");
        runAndExpect("if false", "if(false) print 1;", "");
    }

    @Test
    public void testIfThenElseCondition() {
        runAndExpect("if true", "if(true) print 1; else print 2;", "1\n");
        runAndExpect("if false", "if(false) print 1; else print 2;", "2\n");
    }

    @Test
    public void testWhileLoop() {
        runAndExpect("print 1 2 3", "var i=0; while(i < 3) { i = i + 1; print i;}", "1\n2\n3\n");
    }

    @Test
    public void testForLoop() {
        runAndExpect("print 1 2 3", "for(var i=1; i <= 3; i = i + 1) {print i;}", "1\n2\n3\n");
    }

    @Test
    public void testForNonVarInitLoop() {
        runAndExpect("print 1 2 3", "var i; for(i=1; i <= 3; i = i + 1) {print i;}", "1\n2\n3\n");
    }

    @Test
    public void testTruthiness() {
        runAndExpect("testNilIsFalse", "print !nil;", "true\n");
    }

    @Test
    public void testTruthinessCondition() {
        runAndExpect("testNumbersAreTrue", "if(1) print 3;", "3\n");
        runAndExpect("testStringsAreTrue", "if(\"hello\") print 3;", "3\n");
        runAndExpect("testNilIsFalseInCondtion", "if(nil) print 3;", "");
        runAndExpect("testNilIsFalseInCondtion", "if(nil) print 3; else print 4;", "4\n");
    }

    @Test
    public void testTruthinessWhile() {
        runAndExpect("testNumbersAreTrue", "var a = true; while(a) { print a; a = nil;}", "true\n");
    }

    @Test
    public void testTruthinessFor() {
        runAndExpect("testNumbersAreTrue", "for(var a=true; a ; a = nil) { print a;}", "true\n");
    }

    @Test
    public void testNewArray() {
        runAndExpect("new array", "var a = []; print a;", "[]\n");
    }

    @Test
    public void testGetNil() {
        runAndExpect("get nil", "var a = []; print a[1];", "nil\n");
    }

    @Test
    public void testSetArray() {
        runAndExpect("set a[0]=3", "var a = []; a[0]=3; print a[0];", "3\n");
    }

    @Test
    public void testPrintArray() {
        runAndExpect("print contents", "var a = []; a[0]=3; a[1]=\"hello\"; print a;", "[3, \"hello\"]\n");
    }
}
