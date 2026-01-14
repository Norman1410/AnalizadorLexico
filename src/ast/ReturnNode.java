package ast;

import java.util.*;

public class ReturnNode extends ASTNode {
    private ASTNode expression;

    @Override
    public Map<String, Object> toJsonObject() {
        Map<String, Object> node = createNode("Return");
        if (expression != null)
            node.put("expression", expression.toJsonObject());
        return node;
    }

    public ReturnNode(ASTNode expression, int line, int column) {
        super(line, column);
        this.expression = expression;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("Return");
        if (expression != null)
            expression.print(indent + 1);
    }
}
