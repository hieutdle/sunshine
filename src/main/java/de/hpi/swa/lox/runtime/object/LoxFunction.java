package de.hpi.swa.lox.runtime.object;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.object.DynamicObjectLibrary;

public class LoxFunction implements TruffleObject {

    public final String name;
    private final RootCallTarget callTarget;
    private final MaterializedFrame outerFrame;
    private final LoxObject self;

    public LoxFunction(String name, RootCallTarget callTarget, MaterializedFrame outerFrame, LoxObject self) {
        this.name = name;
        this.callTarget = callTarget;
        this.outerFrame = outerFrame;
        this.self = self;
    }

    public LoxFunction(String name, RootCallTarget callTarget, MaterializedFrame outerFrame) {
        this(name, callTarget, outerFrame, null);
    }

    public LoxFunction(LoxObject obj, LoxFunction m) {
        this(m.name, m.callTarget, m.outerFrame, obj);
    }

    public RootCallTarget getCallTarget() {
        return callTarget;
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

    @CompilerDirectives.TruffleBoundary
    public String toString() {
        return self == null ? "Function " + name : self.klass.name + "#" + name;
    }

    public static LoxObject getThis(VirtualFrame frame) {
        return getCurrentFunction(frame).self;
    }

    public static LoxFunction lookupMethod(LoxObject obj, String name, DynamicObjectLibrary klassDylib) {
        var m = klassDylib.getOrDefault(obj.klass, name, null);
        if (m != null) {
            return new LoxFunction(obj, (LoxFunction) m); // bind method to object
        }
        return null;
    }
}