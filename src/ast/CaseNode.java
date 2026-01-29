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

    @Override
    public void validate(semantics.SymbolTable st) {
        if (st == null)
            return;

        if (expression != null) {
            expression.validate(st);
            String t = expression.getType(st);
            if (!"error".equals(t) && !"unknown".equals(t) && !"boolean".equals(t)) {
                st.addError("Error semántico (línea " + (getLine() + 1) + ", col " + (getColumn() + 1) + "): " +
                        "La expresión de un case debe ser boolean, pero se obtuvo '" + t + "'.");
            }
        }

        if (block != null)
            block.validate(st);
    }

    @Override
    public String getType(semantics.SymbolTable st) {
        return "void";
    }

    public BlockNode getBlock() {
        return (block instanceof BlockNode) ? (BlockNode) block : null;
    }
}
