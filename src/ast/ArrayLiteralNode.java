package ast;

import java.util.*;

public class ArrayLiteralNode extends ASTNode {
    private List<ASTNode> elements;

    public ArrayLiteralNode(List<ASTNode> elements, int line, int column) {
        super(line, column);
        this.elements = elements;
    }

    @Override
    public Map<String, Object> toJsonObject() {
        Map<String, Object> node = createNode("ArrayLiteral");
        List<Map<String, Object>> elementsJson = new ArrayList<>();
        if (elements != null) {
            for (ASTNode e : elements) {
                elementsJson.add(e.toJsonObject());
            }
        }
        node.put("elements", elementsJson);
        return node;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("ArrayLiteral");
        if (elements != null) {
            for (ASTNode e : elements) {
                e.print(indent + 1);
            }
        }
    }

    @Override
    public void validate(semantics.SymbolTable st) {
        if (elements == null || elements.isEmpty())
            return;

        for (ASTNode e : elements) {
            if (e != null)
                e.validate(st);
        }
        String baseType = elements.get(0).getType(st);
        if ("error".equals(baseType) || "unknown".equals(baseType))
            return;

        // Verificar que todos los elementos sean compatibles con el primero
        for (int i = 1; i < elements.size(); i++) {
            String t = elements.get(i).getType(st);
            if (!"error".equals(t) && !"unknown".equals(t)) {
                if (!isCompatible(baseType, t)) {
                    st.addError("Error semántico (línea " + (elements.get(i).getLine() + 1) + ", col "
                            + (elements.get(i).getColumn() + 1) + "): " +
                            "Tipo de elemento incompatible en literal de arreglo. Se esperaba '" + baseType +
                            "' pero se obtuvo '" + t + "'.");
                }
            }
        }
    }

    @Override
    public String getType(semantics.SymbolTable st) {
        if (elements == null || elements.isEmpty())
            return "unknown[]";

        String baseType = elements.get(0).getType(st);
        if ("error".equals(baseType))
            return "error";

        return baseType + "[]";
    }
}
