package ast;

import java.util.*;

public class LoopNode extends ASTNode {
    private ASTNode block;
    private ASTNode exitCondition;

    @Override
    public Map<String, Object> toJsonObject() {
        Map<String, Object> node = createNode("Loop");
        if (block != null)
            node.put("block", block.toJsonObject());
        if (exitCondition != null)
            node.put("exitCondition", exitCondition.toJsonObject());
        return node;
    }

    public LoopNode(ASTNode block, ASTNode exitCondition, int line, int column) {
        super(line, column);
        this.block = block;
        this.exitCondition = exitCondition;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("Loop");
        if (block != null)
            block.print(indent + 1);
        if (exitCondition != null) {
            printIndent(indent + 1);
            System.out.println("Exit When:");
            exitCondition.print(indent + 2);
        }
    }

    @Override
    public void validate(semantics.SymbolTable st) {
        if (st == null)
            return;

        st.openClosedScope("loop@" + getLine() + ":" + getColumn());

        if (block != null)
            block.validate(st);

        if (exitCondition != null) {
            exitCondition.validate(st);
            String t = exitCondition.getType(st);
            if (!"error".equals(t) && !"unknown".equals(t) && !"boolean".equals(t)) {
                st.addError("Error semántico (línea " + (getLine() + 1) + ", col " + (getColumn() + 1) + "): " +
                        "La condición de 'exit when' debe ser boolean, pero se obtuvo '" + t + "'.");
            }
        }

        st.closeViewScope();
    }

    @Override
    public String getType(semantics.SymbolTable st) {
        return "void";
    }

    public BlockNode getBody() {
        return (block instanceof BlockNode) ? (BlockNode) block : null;
    }
}
