package ast;

import java.util.*;

public class DeclNode extends ASTNode {
    private String identifier;
    private ASTNode initializer;
    private boolean isGlobal;
    private String varType;
    private List<Integer> dimensions;

    public DeclNode(String identifier, ASTNode initializer, boolean isGlobal, String varType, List<Integer> dimensions,
            int line, int column) {
        super(line, column);
        this.identifier = identifier;
        this.initializer = initializer;
        this.isGlobal = isGlobal;
        this.varType = varType;
        this.dimensions = dimensions;
    }

    @Override
    public Map<String, Object> toJsonObject() {
        Map<String, Object> node = createNode("Declaration");
        node.put("identifier", identifier);
        node.put("varType", varType);
        if (dimensions != null && !dimensions.isEmpty()) {
            node.put("dimensions", dimensions);
        }
        node.put("isGlobal", isGlobal);
        if (initializer != null)
            node.put("initializer", initializer.toJsonObject());
        return node;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        String dimStr = "";
        if (dimensions != null) {
            for (int d : dimensions)
                dimStr += "[" + d + "]";
        }
        System.out.println(
                (isGlobal ? "Global" : "Local") + " Declaration: " + identifier + " (" + varType + dimStr + ")");
        if (initializer != null)
            initializer.print(indent + 1);
    }

    @Override
    public void validate(semantics.SymbolTable st) {
        if (st == null)
            return;
        if (dimensions != null && !dimensions.isEmpty()) {
            if (!"int".equals(varType) && !"char".equals(varType)) {
                st.addError("Error semántico (línea " + (getLine() + 1) + ", col " + (getColumn() + 1) + "): " +
                        "Solo se permiten arreglos de tipo 'int' o 'char', pero se obtuvo '" + varType + "'.");
            }
        }
        if (initializer != null)
            initializer.validate(st);

        String fullType = getFullType();

        st.declare(new semantics.SymbolInfo(
                identifier,
                fullType,
                isGlobal ? semantics.SymbolKind.GLOBAL_VAR : semantics.SymbolKind.LOCAL_VAR,
                getLine(),
                getColumn(),
                getDims()));

        if (initializer != null) {
            initializer.validate(st);
            String initType = initializer.getType(st);

            if (!"error".equals(initType) && !"unknown".equals(initType)) {
                if (!isCompatible(fullType, initType)) {
                    st.addError("Error semántico (línea " + (getLine() + 1) + ", col " + (getColumn() + 1) + "): " +
                            "Tipo incompatible en la declaración de '" + identifier + "'. Se esperaba '" + fullType +
                            "' pero se obtuvo '" + initType + "'.");
                }
            }
        }
    }

    private String getFullType() {
        StringBuilder sb = new StringBuilder(varType);
        if (dimensions != null) {
            for (int i = 0; i < dimensions.size(); i++) {
                sb.append("[]");
            }
        }
        return sb.toString();
    }

    @Override
    public String getType(semantics.SymbolTable st) {
        return getFullType();
    }

    public String getName() {
        return identifier;
    }

    public String getTypeName() {
        return varType;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public List<Integer> getDims() {
        return (dimensions == null) ? Collections.emptyList() : dimensions;
    }

    public ASTNode getInitializer() {
        return initializer;
    }

}
