package ast;

import java.util.*;

public class CaseNode extends ASTNode {
    private ASTNode expression;
    private ASTNode block;

    public CaseNode(ASTNode expression, ASTNode block, int line, int column) {
        super(line, column);
        this.expression = expression;
        this.block = block;
    }

    @Override
    public Map<String, Object> toJsonObject() {
        Map<String, Object> node = createNode("Case");
        if (expression != null)
            node.put("expression", expression.toJsonObject());
        if (block != null)
            node.put("block", block.toJsonObject());
        return node;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("Case:");
        if (expression != null)
            expression.print(indent + 1);
        if (block != null)
            block.print(indent + 1);
    }
}
