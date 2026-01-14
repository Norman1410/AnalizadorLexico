package ast;

import java.util.*;

public class UnaryNode extends ASTNode {
    private String operator;
    private ASTNode expression;
    private boolean isPrefix;

    public UnaryNode(String operator, ASTNode expression, boolean isPrefix, int line, int column) {
        super(line, column);
        this.operator = operator;
        this.expression = expression;
        this.isPrefix = isPrefix;
    }

    @Override
    public Map<String, Object> toJsonObject() {
        Map<String, Object> node = createNode("UnaryExpression");
        node.put("operator", operator);
        node.put("isPrefix", isPrefix);
        if (expression != null)
            node.put("expression", expression.toJsonObject());
        return node;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("Unary Expression: " + operator + (isPrefix ? " (prefix)" : " (postfix)"));
        if (expression != null)
            expression.print(indent + 1);
    }
}
