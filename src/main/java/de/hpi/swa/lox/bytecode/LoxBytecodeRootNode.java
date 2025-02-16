package de.hpi.swa.lox.bytecode;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Objects;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.bytecode.BytecodeRootNode;
import com.oracle.truffle.api.bytecode.GenerateBytecode;
import com.oracle.truffle.api.bytecode.Operation;
import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.strings.TruffleString;

import de.hpi.swa.lox.LoxLanguage;
import de.hpi.swa.lox.error.LoxRuntimeError;
import de.hpi.swa.lox.nodes.LoxRootNode;
import de.hpi.swa.lox.runtime.LoxContext;

@GenerateBytecode(//
        languageClass = LoxLanguage.class, //
        boxingEliminationTypes = { long.class, boolean.class }, //
        enableUncachedInterpreter = true, //
        enableSerialization = true)
public abstract class LoxBytecodeRootNode extends LoxRootNode implements BytecodeRootNode {

    protected LoxBytecodeRootNode(LoxLanguage language, FrameDescriptor frameDescriptor) {
        super(language, frameDescriptor);
    }

    @Operation
    public static final class LoxPrint {
        @Specialization
        static void doDefault(Object value, @Bind LoxContext context) {
            var out = context.getOutput();
            try {
                out.write(Objects.toString(value).getBytes());
                out.write(System.lineSeparator().getBytes());
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Operation
    public static final class LoxAdd {
        @Specialization
        static Object doLong(long left, long right) {
            try {
                long result = Math.addExact(left, right);
                return result;
            } catch (ArithmeticException e) {
                return BigInteger.valueOf(left).add(BigInteger.valueOf(right));
            }
        }

        @Specialization
        static double doMixed(long left, double right) {
            return left + right;
        }

        @Specialization
        static double doMixed(double left, long right) {
            return left + right;
        }

        @Specialization
        static double doDouble(double left, double right) {
            return left + right;
        }

        @Specialization
        @CompilerDirectives.TruffleBoundary
        static BigInteger doMixed(long left, BigInteger right) {
            return BigInteger.valueOf(left).add(right);
        }

        @Specialization
        @CompilerDirectives.TruffleBoundary
        static BigInteger doMixed(BigInteger left, long right) {
            return left.add(BigInteger.valueOf(right));
        }

        @Specialization
        @CompilerDirectives.TruffleBoundary
        static BigInteger doBigInteger(BigInteger left, BigInteger right) {
            return left.add(right);
        }

        @Specialization
        @CompilerDirectives.TruffleBoundary
        static TruffleString doString(TruffleString left, TruffleString right) {
            String concatenated = left.toJavaStringUncached() + right.toJavaStringUncached();
            return TruffleString.fromJavaStringUncached(concatenated, TruffleString.Encoding.UTF_8);
        }

        // Handle number + string or string + number cases
        @Specialization
        @CompilerDirectives.TruffleBoundary
        static TruffleString doStringAndNumber(TruffleString left, long right) {
            return TruffleString.fromJavaStringUncached(
                    left.toJavaStringUncached() + right,
                    TruffleString.Encoding.UTF_8);
        }

        @Specialization
        @CompilerDirectives.TruffleBoundary
        static TruffleString doStringAndNumber(TruffleString left, double right) {
            return TruffleString.fromJavaStringUncached(
                    left.toJavaStringUncached() + right,
                    TruffleString.Encoding.UTF_8);
        }

        @Specialization
        @CompilerDirectives.TruffleBoundary
        static TruffleString doStringAndBigInteger(TruffleString left, BigInteger right) {
            return TruffleString.fromJavaStringUncached(
                    left.toJavaStringUncached() + right.toString(),
                    TruffleString.Encoding.UTF_8);
        }

        @Specialization
        @CompilerDirectives.TruffleBoundary
        static TruffleString doNumberAndString(long left, TruffleString right) {
            return TruffleString.fromJavaStringUncached(
                    left + right.toJavaStringUncached(),
                    TruffleString.Encoding.UTF_8);
        }

        @Specialization
        @CompilerDirectives.TruffleBoundary
        static TruffleString doNumberAndString(double left, TruffleString right) {
            return TruffleString.fromJavaStringUncached(
                    left + right.toJavaStringUncached(),
                    TruffleString.Encoding.UTF_8);
        }

        @Specialization
        @CompilerDirectives.TruffleBoundary
        static TruffleString doBigIntegerAndString(BigInteger left, TruffleString right) {
            return TruffleString.fromJavaStringUncached(
                    left.toString() + right.toJavaStringUncached(),
                    TruffleString.Encoding.UTF_8);
        }

        @Fallback
        @CompilerDirectives.TruffleBoundary
        static Object typeError(Object left, Object right, @Bind Node node) {
            throw new LoxRuntimeError("Unsupported types for addition: cannot add " + left + " and " + right, node);
        }
    }

    @Operation
    public static final class LoxSub {
        @Specialization
        static Object doLong(long left, long right) {
            try {
                return Math.subtractExact(left, right);
            } catch (ArithmeticException e) {
                return BigInteger.valueOf(left).subtract(BigInteger.valueOf(right));
            }
        }

        @Specialization
        static double doMixed(long left, double right) {
            return left - right;
        }

        @Specialization
        static double doMixed(double left, long right) {
            return left - right;
        }

        @Specialization
        static double doDouble(double left, double right) {
            return left - right;
        }

        @Specialization
        @CompilerDirectives.TruffleBoundary
        static BigInteger doMixed(long left, BigInteger right) {
            return BigInteger.valueOf(left).subtract(right);
        }

        @Specialization
        @CompilerDirectives.TruffleBoundary
        static BigInteger doMixed(BigInteger left, long right) {
            return left.subtract(BigInteger.valueOf(right));
        }

        @Specialization
        @CompilerDirectives.TruffleBoundary
        static BigInteger doBigInteger(BigInteger left, BigInteger right) {
            return left.subtract(right);
        }

        @Fallback
        @CompilerDirectives.TruffleBoundary
        static Object typeError(Object left, Object right, @Bind Node node) {
            throw new LoxRuntimeError("Unsupported types for subtraction: cannot subtract " + left + " and " + right,
                    node);
        }
    }

    @Operation
    public static final class LoxMul {
        @Specialization
        static Object doLong(long left, long right) {
            try {
                return Math.multiplyExact(left, right);
            } catch (ArithmeticException e) {
                return BigInteger.valueOf(left).multiply(BigInteger.valueOf(right));
            }
        }

        @Specialization
        static double doMixed(long left, double right) {
            return left * right;
        }

        @Specialization
        static double doMixed(double left, long right) {
            return left * right;
        }

        @Specialization
        static double doDouble(double left, double right) {
            return left * right;
        }

        @Specialization
        @CompilerDirectives.TruffleBoundary
        static BigInteger doMixed(long left, BigInteger right) {
            return BigInteger.valueOf(left).multiply(right);
        }

        @Specialization
        @CompilerDirectives.TruffleBoundary
        static BigInteger doMixed(BigInteger left, long right) {
            return left.multiply(BigInteger.valueOf(right));
        }

        @Specialization
        @CompilerDirectives.TruffleBoundary
        static BigInteger doBigInteger(BigInteger left, BigInteger right) {
            return left.multiply(right);
        }

        @Fallback
        @CompilerDirectives.TruffleBoundary
        static Object typeError(Object left, Object right, @Bind Node node) {
            throw new LoxRuntimeError("Unsupported types for multiplication: cannot multiply " + left + " and " + right,
                    node);
        }
    }

    @Operation
    public static final class LoxDiv {
        @Specialization
        static Object doLong(long left, long right, @Bind Node node) {
            if (right == 0) {
                throw new LoxRuntimeError("Cannot divide by zero", node);
            }

            try {
                long result = Math.floorDiv(left, right);
                if (left % right == 0) {
                    return result;
                } else {
                    return (double) left / right;
                }
            } catch (ArithmeticException e) {
                return BigInteger.valueOf(left).divide(BigInteger.valueOf(right));
            }
        }

        @Specialization
        static double doDouble(double left, double right, @Bind Node node) {
            if (right == 0) {
                throw new LoxRuntimeError("Cannot divide by zero", node);
            }
            return left / right;
        }

        @Specialization
        static double doMixed(double left, long right, @Bind Node node) {
            if (right == 0) {
                throw new LoxRuntimeError("Cannot divide by zero", node);
            }
            return left / right;
        }

        @Specialization
        static double doMixed(long left, double right, @Bind Node node) {
            if (right == 0) {
                throw new LoxRuntimeError("Cannot divide by zero", node);
            }
            return left / right;
        }

        @Specialization
        @CompilerDirectives.TruffleBoundary
        static BigInteger doMixed(long left, BigInteger right, @Bind Node node) {
            if (right.equals(BigInteger.ZERO)) {
                throw new LoxRuntimeError("Cannot divide by zero", node);
            }
            return BigInteger.valueOf(left).divide(right);
        }

        @Specialization
        @CompilerDirectives.TruffleBoundary
        static BigInteger doMixed(BigInteger left, long right, @Bind Node node) {
            if (right == 0) {
                throw new LoxRuntimeError("Cannot divide by zero", node);
            }
            return left.divide(BigInteger.valueOf(right));
        }

        @Fallback
        @CompilerDirectives.TruffleBoundary
        static Object typeError(Object left, Object right, @Bind Node node) {
            throw new LoxRuntimeError("Unsupported types for division: cannot divide " + left + " and " + right, node);
        }
    }

    @Operation
    public static final class LoxMod {
        @Specialization
        static Object doLong(long left, long right, @Bind Node node) {
            if (right == 0) {
                throw new LoxRuntimeError("Cannot modulo by zero", node);
            }
            return left % right;
        }

        @Specialization
        static double doDouble(double left, double right, @Bind Node node) {
            if (right == 0) {
                throw new LoxRuntimeError("Cannot modulo by zero", node);
            }
            return left % right;
        }

        @Specialization
        @CompilerDirectives.TruffleBoundary
        static BigInteger doBigInteger(BigInteger left, BigInteger right, @Bind Node node) {
            if (right.equals(BigInteger.ZERO)) {
                throw new LoxRuntimeError("Cannot modulo by zero", node);
            }
            return left.mod(right);
        }

        @Specialization
        @CompilerDirectives.TruffleBoundary
        static BigInteger doMixed(long left, BigInteger right, @Bind Node node) {
            if (right.equals(BigInteger.ZERO)) {
                throw new LoxRuntimeError("Cannot modulo by zero", node);
            }
            return BigInteger.valueOf(left).mod(right);
        }

        @Specialization
        @CompilerDirectives.TruffleBoundary
        static BigInteger doMixed(BigInteger left, long right, @Bind Node node) {
            if (right == 0) {
                throw new LoxRuntimeError("Cannot modulo by zero", node);
            }
            return left.mod(BigInteger.valueOf(right));
        }

        @Fallback
        @CompilerDirectives.TruffleBoundary
        static Object typeError(Object left, Object right, @Bind Node node) {
            throw new LoxRuntimeError("Unsupported types for modulus: cannot modulo " + left + " and " + right, node);
        }
    }

    @Operation
    public static final class LoxEqual {
        @Specialization
        @CompilerDirectives.TruffleBoundary
        static boolean doEqual(Object left, Object right) {
            return Objects.equals(left, right);
        }
    }

    @Operation
    public static final class LoxNotEqual {
        @Specialization
        @CompilerDirectives.TruffleBoundary
        static boolean doNotEqual(Object left, Object right) {
            return !Objects.equals(left, right);
        }
    }

    @Operation
    public static final class LoxLessThan {
        @Specialization
        static boolean doLessThan(long left, long right) {
            return left < right;
        }

        @Specialization
        static boolean doMixed(long left, double right) {
            return left < right;
        }

        @Specialization
        static boolean doMixed(double left, long right) {
            return left < right;
        }

        @Specialization
        static boolean doDouble(double left, double right) {
            return left < right;
        }

        @Specialization
        @CompilerDirectives.TruffleBoundary
        static boolean doMixed(long left, BigInteger right) {
            return BigInteger.valueOf(left).compareTo(right) < 0;
        }

        @Specialization
        @CompilerDirectives.TruffleBoundary
        static boolean doMixed(BigInteger left, long right) {
            return left.compareTo(BigInteger.valueOf(right)) < 0;
        }

        @Specialization
        @CompilerDirectives.TruffleBoundary
        static boolean doBigInteger(BigInteger left, BigInteger right) {
            return left.compareTo(right) < 0;
        }

        @Fallback
        @CompilerDirectives.TruffleBoundary
        static boolean typeError(Object left, Object right, @Bind Node node) {
            throw new LoxRuntimeError("Cannot compare " + left + " and " + right, node);
        }
    }

    @Operation
    public static final class LoxGreaterThan {
        @Specialization
        static boolean doGreaterThan(long left, long right) {
            return left > right;
        }

        @Specialization
        static boolean doMixed(long left, double right) {
            return left > right;
        }

        @Specialization
        static boolean doMixed(double left, long right) {
            return left > right;
        }

        @Specialization
        static boolean doDouble(double left, double right) {
            return left > right;
        }

        @Specialization
        @CompilerDirectives.TruffleBoundary
        static boolean doMixed(long left, BigInteger right) {
            return BigInteger.valueOf(left).compareTo(right) > 0;
        }

        @Specialization
        @CompilerDirectives.TruffleBoundary
        static boolean doMixed(BigInteger left, long right) {
            return left.compareTo(BigInteger.valueOf(right)) > 0;
        }

        @Specialization
        @CompilerDirectives.TruffleBoundary
        static boolean doBigInteger(BigInteger left, BigInteger right) {
            return left.compareTo(right) > 0;
        }

        @Fallback
        @CompilerDirectives.TruffleBoundary
        static boolean typeError(Object left, Object right, @Bind Node node) {
            throw new LoxRuntimeError("Cannot compare " + left + " and " + right, node);
        }
    }

    @Operation
    public static final class LoxLessEqual {
        @Specialization
        static boolean doLessEqual(long left, long right) {
            return left <= right;
        }

        @Specialization
        static boolean doMixed(long left, double right) {
            return left <= right;
        }

        @Specialization
        static boolean doMixed(double left, long right) {
            return left <= right;
        }

        @Specialization
        static boolean doDouble(double left, double right) {
            return left <= right;
        }

        @Specialization
        @CompilerDirectives.TruffleBoundary
        static boolean doMixed(long left, BigInteger right) {
            return BigInteger.valueOf(left).compareTo(right) <= 0;
        }

        @Specialization
        @CompilerDirectives.TruffleBoundary
        static boolean doMixed(BigInteger left, long right) {
            return left.compareTo(BigInteger.valueOf(right)) <= 0;
        }

        @Specialization
        @CompilerDirectives.TruffleBoundary
        static boolean doBigInteger(BigInteger left, BigInteger right) {
            return left.compareTo(right) <= 0;
        }

        @Fallback
        @CompilerDirectives.TruffleBoundary
        static boolean typeError(Object left, Object right, @Bind Node node) {
            throw new LoxRuntimeError("Cannot compare " + left + " and " + right, node);
        }
    }

    @Operation
    public static final class LoxGreaterEqual {
        @Specialization
        static boolean doGreaterEqual(long left, long right) {
            return left >= right;
        }

        @Specialization
        static boolean doMixed(long left, double right) {
            return left >= right;
        }

        @Specialization
        static boolean doMixed(double left, long right) {
            return left >= right;
        }

        @Specialization
        static boolean doDouble(double left, double right) {
            return left >= right;
        }

        @Specialization
        @CompilerDirectives.TruffleBoundary
        static boolean doMixed(long left, BigInteger right) {
            return BigInteger.valueOf(left).compareTo(right) >= 0;
        }

        @Specialization
        @CompilerDirectives.TruffleBoundary
        static boolean doMixed(BigInteger left, long right) {
            return left.compareTo(BigInteger.valueOf(right)) >= 0;
        }

        @Specialization
        @CompilerDirectives.TruffleBoundary
        static boolean doBigInteger(BigInteger left, BigInteger right) {
            return left.compareTo(right) >= 0;
        }

        @Fallback
        @CompilerDirectives.TruffleBoundary
        static boolean typeError(Object left, Object right, @Bind Node node) {
            throw new LoxRuntimeError("Cannot compare " + left + " and " + right, node);
        }
    }

    @Operation
    public static final class LoxNeg {
        @Specialization
        static long doLong(long value) {
            return -value;
        }

        @Specialization
        static double doDouble(double value) {
            return -value;
        }

        @Specialization
        @CompilerDirectives.TruffleBoundary
        static BigInteger doBigInteger(BigInteger value) {
            return value.negate();
        }

        @Fallback
        @CompilerDirectives.TruffleBoundary
        static Object typeError(Object value, @Bind Node node) {
            throw new LoxRuntimeError("Unsupported type for negation: cannot negate " + value, node);
        }
    }

    @Operation
    public static final class LoxNot {
        @Specialization
        static boolean doNot(boolean value) {
            return !value;
        }

        @Fallback
        @CompilerDirectives.TruffleBoundary
        static Object typeError(Object value, @Bind Node node) {
            throw new LoxRuntimeError("Unsupported type for logical not: cannot negate " + value, node);
        }
    }

    @Operation
    public static final class LoxOr {
        @Specialization
        static boolean doOr(boolean left, boolean right) {
            return left || right;
        }
    }

    @Operation
    public static final class LoxAnd {
        @Specialization
        static boolean doAnd(boolean left, boolean right) {
            return left && right;
        }
    }
}
