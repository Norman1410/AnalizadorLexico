package ast;

import java.util.*;

public class BreakNode extends ASTNode {
    public BreakNode(int line, int column) {
        super(line, column);
    }

    @Override
    public Map<String, Object> toJsonObject() {
        return createNode("Break");
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("Break");
    }

    @Override
    public void validate(semantics.SymbolTable st) {
        if (st == null)
            return;

        if (!st.isInLoop()) {
            st.addError("Error semántico (línea " + (getLine() + 1) + ", col " + (getColumn() + 1) + "): " +
                    "'break' solo puede usarse dentro de un bucle (loop o for).");
        }
    }

    @Override
    public String getType(semantics.SymbolTable st) {
        return "void";
    }
}
