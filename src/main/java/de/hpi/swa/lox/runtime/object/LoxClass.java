package de.hpi.swa.lox.runtime.object;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Shape;

public class LoxClass extends DynamicObject {
    final String name;
    static final Shape classShape = Shape.newBuilder().allowImplicitCastIntToLong(true).build();
    public final Shape instanceShape = Shape.newBuilder()
            .addConstantProperty("Class", this, 0)
            .allowImplicitCastIntToLong(true).build();

    public LoxClass(String name) {
        super(classShape);
        this.name = name;
    }

    @CompilerDirectives.TruffleBoundary
    @Override
    public String toString() {
        return "Class " + name;
    }
}