package ast;

import java.util.*;

public class BinaryNode extends ASTNode {
    private ASTNode left;
    private String operator;
    private ASTNode right;

    @Override
    public Map<String, Object> toJsonObject() {
        Map<String, Object> node = createNode("BinaryExpression");
        node.put("operator", operator);
        if (left != null)
            node.put("left", left.toJsonObject());
        if (right != null)
            node.put("right", right.toJsonObject());
        return node;
    }

    public BinaryNode(ASTNode left, String operator, ASTNode right, int line, int column) {
        super(line, column);
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("Binary Expression: " + operator);
        if (left != null)
            left.print(indent + 1);
        if (right != null)
            right.print(indent + 1);
    }
}
