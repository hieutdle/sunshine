package de.hpi.swa.lox.runtime.object;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;

public class LoxFunction {
    private final String name;
    private final RootCallTarget callTarget;
    private final MaterializedFrame outerFrame;

    public LoxFunction(String name, RootCallTarget callTarget, MaterializedFrame outerFrame) {
        this.name = name;
        this.callTarget = callTarget;
        this.outerFrame = outerFrame;
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

    static LoxFunction getCurrentFunction(Frame frame) {
        return (LoxFunction) frame.getArguments()[0];
    }

    static public MaterializedFrame getFrameAtLevelN(VirtualFrame frame, int index) {
        assert index > 0;
        LoxFunction func = getCurrentFunction(frame);
        for (int i = index - 1; i > 0; i--) {
            func = getCurrentFunction(func.outerFrame);
        }
        return func.outerFrame;
    }
}