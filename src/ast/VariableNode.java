package ast;

import java.util.*;

public class VariableNode extends ASTNode {
    private String identifier;

    public VariableNode(String identifier, int line, int column) {
        super(line, column);
        this.identifier = identifier;
    }

    public String getName() {
        return identifier;
    }

    @Override
    public Map<String, Object> toJsonObject() {
        Map<String, Object> node = createNode("Variable");
        node.put("name", identifier);
        return node;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("Variable: " + identifier);
    }
}
