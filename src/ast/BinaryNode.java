package ast;

import java.util.*;

public class BinaryNode extends ASTNode {
    private ASTNode left;
    private String operator;
    private ASTNode right;

    @Override
    public Map<String, Object> toJsonObject() {
        Map<String, Object> node = createNode("BinaryExpression");
        node.put("operator", operator);
        if (left != null)
            node.put("left", left.toJsonObject());
        if (right != null)
            node.put("right", right.toJsonObject());
        return node;
    }

    public BinaryNode(ASTNode left, String operator, ASTNode right, int line, int column) {
        super(line, column);
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("Binary Expression: " + operator);
        if (left != null)
            left.print(indent + 1);
        if (right != null)
            right.print(indent + 1);
    }

    @Override
    public void validate(semantics.SymbolTable st) {
        if (left != null)
            left.validate(st);
        if (right != null)
            right.validate(st);

        // fuerza el cálculo para que, si hay error, se agregue al st
        getType(st);
    }

    @Override
    public String getType(semantics.SymbolTable st) {
        String lt = (left == null) ? "error" : safeType(left.getType(st));
        String rt = (right == null) ? "error" : safeType(right.getType(st));

        if ("error".equals(lt) || "error".equals(rt))
            return "error";
        if ("unknown".equals(lt) || "unknown".equals(rt))
            return "unknown";

        String op = (operator == null) ? "" : operator;

        // Aritméticos: + - * / // % ^
        if (op.equals("+") || op.equals("-") || op.equals("*") || op.equals("/") ||
                op.equals("//") || op.equals("%") || op.equals("^")) {

            if (!isNumeric(lt) || !isNumeric(rt)) {
                st.addError("Error semántico (línea " + (getLine() + 1) + ", col " + (getColumn() + 1) + "): " +
                        "El operador '" + op + "' requiere operandos numéricos, pero se obtuvo '" + lt + "' y '" + rt
                        + "'.");
                return "error";
            }

            // Reglas específicas
            if (op.equals("%")) {
                if (!lt.equals("int") || !rt.equals("int")) {
                    st.addError("Error semántico (línea " + (getLine() + 1) + ", col " + (getColumn() + 1) + "): " +
                            "El operador '%' solo es válido para enteros (int), pero se obtuvo '" + lt + "' y '" + rt
                            + "'.");
                    return "error";
                }
                return "int";
            }
            if (op.equals("//"))
                return "int";
            if (op.equals("^"))
                return "float";

            // Promoción estándar para + - * /
            if (lt.equals("float") || rt.equals("float"))
                return "float";
            return "int";
        }

        // Relacionales: < <= > >= => boolean (solo numéricos)
        if (op.equals("<") || op.equals("<=") || op.equals(">") || op.equals(">=")) {
            if (!isNumeric(lt) || !isNumeric(rt)) {
                st.addError("Error semántico (línea " + (getLine() + 1) + ", col " + (getColumn() + 1) + "): " +
                        "El operador '" + op + "' requiere operandos numéricos, pero se obtuvo '" + lt + "' y '" + rt
                        + "'.");
                return "error";
            }
            return "boolean";
        }

        // Igualdad: == != => boolean
        if (op.equals("==") || op.equals("!=")) {
            // Permitimos int==float como comparación numérica
            if (isNumeric(lt) && isNumeric(rt))
                return "boolean";

            if (!lt.equals(rt)) {
                st.addError("Error semántico (línea " + (getLine() + 1) + ", col " + (getColumn() + 1) + "): " +
                        "El operador '" + op + "' requiere tipos compatibles, pero se obtuvo '" + lt + "' y '" + rt
                        + "'.");
                return "error";
            }
            return "boolean";
        }

        // Lógicos: && || => boolean
        if (op.equals("&&") || op.equals("||")) {
            if (!lt.equals("boolean") || !rt.equals("boolean")) {
                st.addError("Error semántico (línea " + (getLine() + 1) + ", col " + (getColumn() + 1) + "): " +
                        "El operador '" + op + "' requiere operandos booleanos, pero se obtuvo '" + lt + "' y '" + rt
                        + "'.");
                return "error";
            }
            return "boolean";
        }

        st.addError("Error semántico (línea " + (getLine() + 1) + ", col " + (getColumn() + 1) + "): " +
                "Operador binario desconocido '" + op + "'.");
        return "error";
    }

    private String safeType(String t) {
        if (t == null)
            return "unknown";
        if (t.equals("bool"))
            return "boolean";
        return t;
    }

}
