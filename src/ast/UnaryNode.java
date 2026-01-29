package ast;

import java.util.*;

public class UnaryNode extends ASTNode {
    private String operator;
    private ASTNode expression;
    private boolean isPrefix;

    public UnaryNode(String operator, ASTNode expression, boolean isPrefix, int line, int column) {
        super(line, column);
        this.operator = operator;
        this.expression = expression;
        this.isPrefix = isPrefix;
    }

    @Override
    public Map<String, Object> toJsonObject() {
        Map<String, Object> node = createNode("UnaryExpression");
        node.put("operator", operator);
        node.put("isPrefix", isPrefix);
        if (expression != null)
            node.put("expression", expression.toJsonObject());
        return node;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("Unary Expression: " + operator + (isPrefix ? " (prefix)" : " (postfix)"));
        if (expression != null)
            expression.print(indent + 1);
    }

    @Override
    public void validate(semantics.SymbolTable st) {
        if (expression != null)
            expression.validate(st);

        String t = getType(st);
        if (t.equals("error"))
            return;

        // Validaciones específicas de operadores
        if (operator.equals("++") || operator.equals("--")) {
            if (!(expression instanceof VariableNode) && !(expression instanceof ArrayAccessNode)) {
                st.addError("Error semántico (línea " + (getLine() + 1) + ", col " + (getColumn() + 1) + "): " +
                        "El operador '" + operator
                        + "' solo puede aplicarse a variables o elementos de arreglo (lvalues).");
            }
        }
    }

    @Override
    public String getType(semantics.SymbolTable st) {
        if (expression == null)
            return "error";

        String t = expression.getType(st);
        if (t.equals("error") || t.equals("unknown"))
            return t;

        if (operator == null)
            return t;
        if (operator.equals("!")) {
            if (!t.equals("boolean")) {
                st.addError("Error semántico (línea " + (getLine() + 1) + ", col " + (getColumn() + 1) + "): " +
                        "El operador '!' requiere un operando booleano, pero se obtuvo '" + t + "'.");
                return "error";
            }
            return "boolean";
        }
        if (operator.equals("+") || operator.equals("-") || operator.equals("++") ||
                operator.equals("--") || operator.equals("neg")) {
            if (!t.equals("int") && !t.equals("float")) {
                st.addError("Error semántico (línea " + (getLine() + 1) + ", col " + (getColumn() + 1) + "): " +
                        "El operador '" + operator + "' requiere un operando numérico, pero se obtuvo '" + t + "'.");
                return "error";
            }
            return t;
        }

        return t;
    }
}
