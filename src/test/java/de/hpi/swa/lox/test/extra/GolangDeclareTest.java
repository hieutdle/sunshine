package de.hpi.swa.lox.test.extra;

import org.junit.Test;

import de.hpi.swa.lox.test.AbstractLoxTest;

public class GolangDeclareTest extends AbstractLoxTest {

    @Test
    public void testNewDeclaration() {
        runAndExpect("new declaration", "a := 3; print a;", "3\n");
    }

    @Test
    public void testNewDeclarationWithExpression() {
        runAndExpect("new declaration with expression", "a := 2 + 3; print a;", "5\n");
    }
}
