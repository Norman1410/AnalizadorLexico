package ast;

import java.util.*;

public class VariableNode extends ASTNode {
    private String identifier;

    public VariableNode(String identifier, int line, int column) {
        super(line, column);
        this.identifier = identifier;
    }

    public String getName() {
        return identifier;
    }

    @Override
    public Map<String, Object> toJsonObject() {
        Map<String, Object> node = createNode("Variable");
        node.put("name", identifier);
        return node;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("Variable: " + identifier);
    }

    @Override
    public void validate(semantics.SymbolTable st) {
        if (st.lookup(identifier) == null) {
            st.addError("Error semántico: Variable '" + identifier + "' no declarada (línea=" + (line + 1) + ", col="
                    + (column + 1) + ")");
        }
    }

    @Override
    public String getType(semantics.SymbolTable st) {
        semantics.SymbolInfo si = st.lookup(identifier);
        return (si != null) ? si.type : "unknown";
    }
}
