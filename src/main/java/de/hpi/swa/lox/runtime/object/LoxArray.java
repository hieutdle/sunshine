package de.hpi.swa.lox.runtime.object;

import java.util.Arrays;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.strings.TruffleString;

public class LoxArray {
    private Object[] elements;
    int size = 0;

    public LoxArray() {
        elements = new Object[10];
    }

    public Object get(int index) {
        if (elements.length <= index || index < 0) {
            return Nil.INSTANCE;
        }
        var result = elements[index];
        if (result != null) {
            return result;
        } else {
            return Nil.INSTANCE;
        }
    }

    public void set(int index, Object value) {
        if (index >= size) {
            size = index + 1;
        }
        if (index >= elements.length) {
            this.ensureCapacity();
        }
        elements[index] = value;
    }

    @CompilerDirectives.TruffleBoundary
    private void ensureCapacity() {
        elements = Arrays.copyOf(elements, Math.max(elements.length, size) * 2);
    }

    @CompilerDirectives.TruffleBoundary
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < size; i++) {
            if (i > 0)
                sb.append(", ");
            Object element = elements[i];
            if (element instanceof TruffleString) {
                sb.append("\"").append(element).append("\"");
            } else if (element instanceof Nil) {
                sb.append("nil");
            } else {
                sb.append(element);
            }
        }
        sb.append("]");
        return sb.toString();
    }
}