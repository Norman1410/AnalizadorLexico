package ast;

import java.util.*;

public class ShowNode extends ASTNode {
    private ASTNode expression;

    @Override
    public Map<String, Object> toJsonObject() {
        Map<String, Object> node = createNode("Show");
        if (expression != null)
            node.put("expression", expression.toJsonObject());
        return node;
    }

    public ShowNode(ASTNode expression, int line, int column) {
        super(line, column);
        this.expression = expression;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("Show");
        if (expression != null)
            expression.print(indent + 1);
    }

    @Override
    public void validate(semantics.SymbolTable st) {
    }

    @Override
    public String getType(semantics.SymbolTable st) {
        return "void";
    }
}
