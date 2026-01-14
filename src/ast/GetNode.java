package ast;

import java.util.*;

public class GetNode extends ASTNode {
    private VariableNode target;

    public GetNode(VariableNode target, int line, int column) {
        super(line, column);
        this.target = target;
    }

    @Override
    public Map<String, Object> toJsonObject() {
        Map<String, Object> node = createNode("Get");
        node.put("target", target.toJsonObject());
        return node;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("Get:");
        target.print(indent + 1);
    }
}
