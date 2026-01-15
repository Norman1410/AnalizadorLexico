package ast;

import java.util.*;

public class ParamNode extends ASTNode {
    private final String name;
    private final String type;

    public ParamNode(String name, String type, int line, int column) {
        super(line, column);
        this.name = name;
        this.type = type;
    }

    public String getName() { return name; }

    public String getType() { return type; }

    @Override
    public Map<String, Object> toJsonObject() {
        Map<String, Object> node = createNode("Parameter");
        node.put("name", name);
        node.put("type", type);
        return node;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("Parameter: " + name + " (" + type + ")");
    }
}
