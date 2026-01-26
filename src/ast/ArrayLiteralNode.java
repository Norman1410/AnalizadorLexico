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
    }

    @Override
    public String getType(semantics.SymbolTable st) {
        return "unknown";
    }
}
