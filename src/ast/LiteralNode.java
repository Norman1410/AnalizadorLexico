package ast;

import java.util.*;

public class LiteralNode extends ASTNode {
    private Object value;
    private String type;

    public LiteralNode(Object value, String type, int line, int column) {
        super(line, column);
        this.value = value;
        this.type = type;
    }

    @Override
    public Map<String, Object> toJsonObject() {
        Map<String, Object> node = createNode("Literal");
        node.put("valueType", type);

        if (type.equals("string") && value instanceof String) {
            String str = (String) value;
            node.put("lexeme", str);
            if (str.startsWith("\"") && str.endsWith("\"") && str.length() >= 2) {
                node.put("value", str.substring(1, str.length() - 1));
            } else {
                node.put("value", str);
            }
        } else {
            node.put("value", value);
        }

        return node;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("Literal (" + type + "): " + value);
    }
}
