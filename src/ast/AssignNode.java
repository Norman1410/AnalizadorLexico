package ast;

import java.util.*;

public class AssignNode extends ASTNode {
    private ASTNode target;;
    private ASTNode expression;

    public AssignNode(ASTNode target, ASTNode expression, int line, int column) {
        super(line, column);
        this.target = target;
        this.expression = expression;
    }

    @Override
    public Map<String, Object> toJsonObject() {
        Map<String, Object> node = createNode("Assignment");
        node.put("target", target.toJsonObject());
        if (expression != null)
            node.put("expression", expression.toJsonObject());
        return node;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("Assignment:");
        target.print(indent + 1);
        printIndent(indent + 1);
        System.out.println("->");
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

    public ASTNode getTarget() {
        return target;
    }

    public ASTNode getExpression() {
        return expression;
    }
}
