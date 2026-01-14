package ast;

import java.util.*;

public class BreakNode extends ASTNode {
    public BreakNode(int line, int column) {
        super(line, column);
    }

    @Override
    public Map<String, Object> toJsonObject() {
        return createNode("Break");
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("Break");
    }
}
