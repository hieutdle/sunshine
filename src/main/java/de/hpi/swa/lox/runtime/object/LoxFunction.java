package de.hpi.swa.lox.runtime.object;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.frame.VirtualFrame;

public class LoxFunction {
    private final String name;
    private final RootCallTarget callTarget;

    public LoxFunction(String name, RootCallTarget callTarget) {
        this.name = name;
        this.callTarget = callTarget;
    }

    public RootCallTarget getCallTarget() {
        return callTarget;
    }

    @CompilerDirectives.TruffleBoundary
    @Override
    public String toString() {
        return "Function " + this.name;
    }

    public Object[] createArguments(Object[] userArguments) {
        Object[] result = new Object[userArguments.length + 1];
        System.arraycopy(userArguments, 0, result, 1, userArguments.length);
        result[0] = this;
        return result;
    }

    static Object getArgument(VirtualFrame frame, int index) {
        return frame.getArguments()[index + 1];
    }
}