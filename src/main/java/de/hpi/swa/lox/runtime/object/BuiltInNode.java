package de.hpi.swa.lox.runtime.object;





import com.oracle.truffle.api.nodes.RootNode;

public abstract class BuiltInNode extends RootNode {
    BuiltInNode() {
        super(null);
    }
}