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

    public VariableNode getArray() { return array; }
    public List<ASTNode> getIndices() { return indices; }

    @Override
    public Map<String, Object> toJsonObject() {
        Map<String, Object> node = createNode("ArrayAccess");
        node.put("array", array.toJsonObject());

        List<Map<String, Object>> idx = new ArrayList<>();
        if (indices != null) for (ASTNode e : indices) idx.add(e.toJsonObject());
        node.put("indices", idx);

        return node;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("ArrayAccess:");
        array.print(indent + 1);
        if (indices != null) for (ASTNode e : indices) e.print(indent + 1);
    }
}
