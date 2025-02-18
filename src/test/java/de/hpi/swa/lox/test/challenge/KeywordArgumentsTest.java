// package de.hpi.swa.lox.test.challenge;

// import org.junit.Test;

// import de.hpi.swa.lox.test.AbstractLoxTest;

// public class KeywordArgumentsTest extends AbstractLoxTest {

//     @Test
//     public void testFunctionWithPositionalArguments() {
//         runAndExpect("function with positional arguments",
//                 "fun f(a, b) { print a + b; } f(3, 4);",
//                 "7\n");
//     }

//     @Test
//     public void testFunctionWithKeywordArguments() {
//         runAndExpect("function with keyword arguments",
//                 "fun f(a, b) { print a + b; } f(a: 3, b: 4);",
//                 "7\n");
//     }

//     @Test
//     public void testFunctionWithMixedArguments() {
//         runAndExpect("function with mixed arguments",
//                 "fun f(a, b) { print a * b; } f(3, b: 4);",
//                 "12\n");
//     }

//     @Test
//     public void testFunctionWithReorderedKeywordArguments() {
//         runAndExpect("function with reordered keyword arguments",
//                 "fun f(a, b) { print a - b; } f(b: 4, a: 10);",
//                 "6\n");
//     }

// }