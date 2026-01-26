package ast;

import java.util.*;

public class ExitWhenNode extends ASTNode {
    private ASTNode condition;

    public ExitWhenNode(ASTNode condition, int line, int column) {
        super(line, column);
        this.condition = condition;
    }

    @Override
    public Map<String, Object> toJsonObject() {
        Map<String, Object> node = createNode("ExitWhen");
        if (condition != null) {
            node.put("condition", condition.toJsonObject());
        }
        return node;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("Exit When:");
        if (condition != null) {
            condition.print(indent + 1);
        }
    }

    @Override
    public void validate(semantics.SymbolTable st) {
    }

    @Override
    public String getType(semantics.SymbolTable st) {
        return "void";
    }
}
