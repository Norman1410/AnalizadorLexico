package ast;

import java.util.*;

public class ForNode extends ASTNode {
    private ASTNode init;
    private ASTNode condition;
    private ASTNode step;
    private ASTNode block;

    public ForNode(ASTNode init, ASTNode condition, ASTNode step, ASTNode block, int line, int column) {
        super(line, column);
        this.init = init;
        this.condition = condition;
        this.step = step;
        this.block = block;
    }

    @Override
    public Map<String, Object> toJsonObject() {
        Map<String, Object> node = createNode("For");
        if (init != null)
            node.put("init", init.toJsonObject());
        if (condition != null)
            node.put("condition", condition.toJsonObject());
        if (step != null)
            node.put("step", step.toJsonObject());
        if (block != null)
            node.put("block", block.toJsonObject());
        return node;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("For");
        if (init != null) {
            printIndent(indent + 1);
            System.out.println("Init:");
            init.print(indent + 2);
        }
        if (condition != null) {
            printIndent(indent + 1);
            System.out.println("Condition:");
            condition.print(indent + 2);
        }
        if (step != null) {
            printIndent(indent + 1);
            System.out.println("Step:");
            step.print(indent + 2);
        }
        if (block != null)
            block.print(indent + 1);
    }

    @Override
    public void validate(semantics.SymbolTable st) {
        if (st == null)
            return;

        st.openClosedScope("for@" + getLine() + ":" + getColumn());

        if (init != null)
            init.validate(st);

        if (condition != null) {
            condition.validate(st);
            String ct = condition.getType(st);
            if (!"error".equals(ct) && !"unknown".equals(ct) && !"boolean".equals(ct)) {
                st.addError("Error semántico (línea " + (getLine() + 1) + ", col " + (getColumn() + 1) + "): " +
                        "La condición de 'for' debe ser boolean, pero se obtuvo '" + ct + "'.");
            }
        }

        if (step != null)
            step.validate(st);
        if (block != null)
            block.validate(st);

        st.closeViewScope();
    }

    @Override
    public String getType(semantics.SymbolTable st) {
        return "void";
    }

    public BlockNode getBody() {
        return (block instanceof BlockNode) ? (BlockNode) block : null;
    }

    public ASTNode getInit() {
        return init;
    }
}
