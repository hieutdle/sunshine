package de.hpi.swa.lox.bytecode;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.bytecode.BytecodeNode;
import com.oracle.truffle.api.bytecode.BytecodeRootNode;
import com.oracle.truffle.api.bytecode.ConstantOperand;
import com.oracle.truffle.api.bytecode.GenerateBytecode;
import com.oracle.truffle.api.bytecode.LocalAccessor;
import com.oracle.truffle.api.bytecode.Operation;
import com.oracle.truffle.api.bytecode.Variadic;
import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.strings.TruffleString;

import de.hpi.swa.lox.LoxLanguage;
import de.hpi.swa.lox.error.LoxRuntimeError;
import de.hpi.swa.lox.nodes.LoxRootNode;
import de.hpi.swa.lox.runtime.LoxContext;
import de.hpi.swa.lox.runtime.object.GlobalObject;
import de.hpi.swa.lox.runtime.object.LoxArray;
import de.hpi.swa.lox.runtime.object.LoxFunction;
import de.hpi.swa.lox.runtime.object.Nil;

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
            throw new LoxRuntimeError("Cannot compare " + left + "of type " + left.getClass().getSimpleName() + " and "
                    + right + "of type " + right.getClass().getSimpleName(), node);
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
        static Object fallback(Object value, @Bind Node node) {
            return !isTruthy(value);
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

    @CompilerDirectives.TruffleBoundary
    static Object checkDeclared(String name, GlobalObject globalObject, Node node) {
        if (!globalObject.hasKey(name)) {
            throw new LoxRuntimeError("Variable " + name + " was not declared", node);
        }
        return globalObject.get(name);
    }

    @Operation
    @ConstantOperand(type = String.class)
    public static final class LoxWriteGlobalVariable {
        @Specialization
        static void doDefault(String name, Object value,
                @Bind LoxContext context,
                @Bind Node node) {
            GlobalObject globalObject = context.getGlobalObject();
            checkDeclared(name, globalObject, node);
            globalObject.set(name, value);
        }
    }

    @Operation
    @ConstantOperand(type = String.class)
    public static final class LoxReadGlobalVariable {
        @Specialization
        static Object doDefault(String name,
                @Bind LoxContext context,
                @Bind Node node) {
            GlobalObject globalObject = context.getGlobalObject();
            var result = checkDeclared(name, globalObject, node);
            if (result == null) {
                throw new LoxRuntimeError("Variable " + name + " was not defined", node);
            }
            return result;
        }
    }

    @Operation
    @ConstantOperand(type = String.class)
    public static final class LoxDefineGlobalVariable {
        @Specialization
        static void doDefault(String name,
                @Bind LoxContext context,
                @Bind Node node) {
            GlobalObject globalObject = context.getGlobalObject();
            if (globalObject.get(name) != null) {
                printWarning(name, context);
            }
            globalObject.set(name, null);
        }

        @CompilerDirectives.TruffleBoundary
        private static void printWarning(String name, LoxContext context) {
            var out = context.getOutput();
            try {
                out.write(("Warning: Variable " + name +
                        " was already declared").getBytes());
                out.write(System.lineSeparator().getBytes());
                out.flush();
            } catch (IOException e) {
                // pass
            }
        }
    }

    @Operation
    @ConstantOperand(type = LocalAccessor.class)
    public static final class LoxCheckLocalDefined {
        @Specialization
        static void doDefault(VirtualFrame frame, LocalAccessor accessor,
                @Bind BytecodeNode bytecodeNode,
                @Bind LoxContext context,
                @Bind Node node) {
            if (accessor.isCleared(bytecodeNode, frame)) {
                throw new LoxRuntimeError("Variable " + accessor.toString() + " was not defined", node);
            }
        }
    }

    static private boolean isTruthy(Object object) {
        if (object == Nil.INSTANCE)
            return false;
        if (object instanceof Boolean)
            return (boolean) object;
        return true;
    }

    @Operation
    public static final class LoxIsTruthy {
        @Specialization
        static boolean bool(boolean value) {
            return value == true;
        }

        @Fallback
        static boolean doDefault(Object value) {
            return isTruthy(value);
        }
    }

    @Operation
    public static final class LoxNewArray {
        @Specialization
        static Object fallback() {
            return new LoxArray();
        }
    }

    @Operation
    public static final class LoxArrayLiterals {
        @Specialization
        static Object doDefault(@Variadic Object[] elements) {
            // Always construct a new array from the variadic input
            Object[] array = Arrays.copyOf(elements, elements.length);
            return new LoxArray(array); // Ensure LoxArray receives a proper array
        }
    }

    @Operation
    public static final class LoxReadArray {
        @Specialization(guards = "index >= 0")
        static Object readArray(LoxArray array, long index) {
            return array.get((int) index);
        }
    }

    @Operation
    public static final class LoxWriteArray {
        @Specialization(guards = "index >= 0")
        static Void writeArray(LoxArray array, long index, Object value) {
            array.set((int) index, value);
            return null;
        }
    }

    @Operation
    public static final class LoxArraySize {
        @Specialization
        static long doLoxArray(LoxArray array) {
            return array.getSize();
        }

        @Fallback
        @CompilerDirectives.TruffleBoundary
        static int doError(Object array, @Bind Node node) {
            throw new LoxRuntimeError("Expected array, got " + array.getClass().getSimpleName(), node);
        }
    }

    @Operation
    @ConstantOperand(type = int.class)
    public static final class LoxLoadArgument {
        @Specialization(guards = "index <= arguments.length")
        static Object doDefault(VirtualFrame frame, int index,
                @Bind("frame.getArguments()") Object[] arguments) {
            return arguments[index + 1];
        }

        @Fallback
        static Object doLoadOutOfBounds(int index) {
            /* Use the default null value. */
            return Nil.INSTANCE;
        }
    }

    @Operation
    public static final class LoxLoadVariableArguments {
        @Specialization
        static Object doDefault(VirtualFrame frame, int index,
                @Bind("frame.getArguments()") Object[] arguments) {
            return new LoxArray(Arrays.copyOfRange(arguments, index + 1, arguments.length));
        }
    }

    @Operation
    @ConstantOperand(type = String.class)
    @ConstantOperand(type = RootCallTarget.class)
    @ConstantOperand(type = int.class)
    public static final class LoxCreateFunction {
        @Specialization
        static LoxFunction doDefault(VirtualFrame frame, String name, RootCallTarget callTarget, int frameLevel) {
            MaterializedFrame materializedFrame = frameLevel > 0 ? frame.materialize() : null;
            return new LoxFunction(name, callTarget, materializedFrame);
        }
    }

    @Operation
    public static final class LoxCall {
        @Specialization(limit = "3", //
                guards = "function.getCallTarget() == cachedTarget")
        protected static Object doDirect(LoxFunction function, @Variadic Object[] arguments,
                @Cached("function.getCallTarget()") RootCallTarget cachedTarget,
                @Cached("create(cachedTarget)") DirectCallNode directCallNode) {
            return directCallNode.call(function, function.createArguments(arguments));
        }

        @Specialization(replaces = "doDirect")
        static Object doIndirect(LoxFunction function, @Variadic Object[] arguments,
                @Cached IndirectCallNode callNode) {
            return callNode.call(function.getCallTarget(), function.createArguments(arguments));
        }

        @CompilerDirectives.TruffleBoundary
        @Specialization
        static Object doDefault(Object obj, @Variadic Object[] arguments, @Bind Node node) {
            throw new LoxRuntimeError("cannot call " + obj, node);
        }
    }

    @Operation
    @ConstantOperand(type = int.class)
    public static final class LoxLoadMaterialzedFrameN {
        @Specialization
        public static MaterializedFrame doDefault(VirtualFrame frame, int index) {
            return LoxFunction.getFrameAtLevelN(frame, index);
        }
    }
}
