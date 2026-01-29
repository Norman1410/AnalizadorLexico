package ast;

import java.util.*;

public class ArrayAccessNode extends ASTNode {
    private VariableNode array;
    private List<ASTNode> indices; // 1D o 2D

    public ArrayAccessNode(VariableNode array, List<ASTNode> indices, int line, int column) {
        super(line, column);
        this.array = array;
        this.indices = indices;
    }

    public VariableNode getArray() {
        return array;
    }

    public List<ASTNode> getIndices() {
        return indices;
    }

    @Override
    public Map<String, Object> toJsonObject() {
        Map<String, Object> node = createNode("ArrayAccess");
        node.put("array", array.toJsonObject());

        List<Map<String, Object>> idx = new ArrayList<>();
        if (indices != null)
            for (ASTNode e : indices)
                idx.add(e.toJsonObject());
        node.put("indices", idx);

        return node;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("ArrayAccess:");
        array.print(indent + 1);
        if (indices != null)
            for (ASTNode e : indices)
                e.print(indent + 1);
    }

    @Override
    public void validate(semantics.SymbolTable st) {
        // Validar el identificador del arreglo
        if (array != null)
            array.validate(st);

        // Validar cada índice
        if (indices != null) {
            for (ASTNode idx : indices) {
                if (idx != null) {
                    idx.validate(st);

                    String t = idx.getType(st);
                    if (!"error".equals(t) && !"unknown".equals(t) && !"int".equals(t)) {
                        st.addError("Error semántico (línea " + (idx.getLine() + 1) + ", col " + (idx.getColumn() + 1)
                                + "): " +
                                "El índice de un arreglo debe ser int, pero se obtuvo '" + t + "'.");
                    }
                }
            }
        }
    }

    @Override
    public String getType(semantics.SymbolTable st) {
        if (array == null)
            return "error";

        String t = array.getType(st); // ej: "int[][]"
        if ("error".equals(t) || "unknown".equals(t))
            return t;

        int k = (indices == null) ? 0 : indices.size();
        String originalType = t;

        // Descontar dimensiones por cada índice usado
        while (k > 0 && t.endsWith("[]")) {
            t = t.substring(0, t.length() - 2);
            k--;
        }

        if (k > 0) {
            st.addError("Error semántico (línea " + (getLine() + 1) + ", col " + (getColumn() + 1) + "): " +
                    "Se están usando demasiados índices para el tipo '" + originalType + "'.");
            return "error";
        }

        return t;
    }
}
