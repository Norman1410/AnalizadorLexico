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

    @Override
    public void validate(semantics.SymbolTable st) {
        // es solo validar que el tipo exista
        String t = normalizeType(type);
        if (t.equals("unknown")) {
            st.addError("Tipo de literal desconocido '" + type + "' (line=" + (getLine() + 1) +
                    ", col=" + (getColumn() + 1) + ")");
        }
    }

    @Override
    public String getType(semantics.SymbolTable st) {
        return normalizeType(type);
    }

    private String normalizeType(String raw) {
        if (raw == null)
            return "unknown";
        String t = raw.trim().toLowerCase();
        if (t.equals("bool"))
            return "boolean";
        if (t.equals("boolean"))
            return "boolean";
        if (t.equals("int") || t.equals("float") || t.equals("string") || t.equals("char"))
            return t;
        return "unknown";
    }

    public boolean isString() {
        return "string".equalsIgnoreCase(type);
    }

    public boolean isInt() {
        return "int".equalsIgnoreCase(type);
    }

    public String getStringValue() {
        if (value == null)
            return "";
        String s = String.valueOf(value);
        // por si viene con comillas
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    public int getIntValue() {
        if (value instanceof Integer)
            return (Integer) value;
        if (value instanceof String) {
            String s = (String) value;
            try {
                return Integer.parseInt(s.replace("\"", "").trim());
            } catch (Exception ignored) {
            }
        }
        return 0;
    }
}
