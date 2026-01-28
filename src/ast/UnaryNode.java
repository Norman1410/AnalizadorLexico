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
        if (expression != null) expression.validate(st);

        // Tipado fuerte básico: operadores unarios numéricos requieren int/float
        String t = (expression == null) ? "error" : expression.getType(st);
        if (t == null) t = "error";

        if (operator != null && (operator.equals("-") || operator.equals("+") || operator.equals("neg"))) {
            if (!t.equals("int") && !t.equals("float") && !t.equals("error")) {
                st.addError(
                        "Error semántico (línea " + (getLine() + 1) + ", columna " + (getColumn() + 1) + "): " +
                                "El operador unario '" + operator + "' requiere operando numérico (int/float), pero se obtuvo '" + t + "'."
                );
            }
        }
    }

    @Override
    public String getType(semantics.SymbolTable st) {
        if (expression == null) return "error";

        String t = expression.getType(st);
        if (t == null) return "error";
        if (t.equals("error")) return "error";

        // Para + / - / neg, el tipo es el mismo que el operando (si es numérico)
        if (operator != null && (operator.equals("-") || operator.equals("+") || operator.equals("neg"))) {
            if (t.equals("int") || t.equals("float")) return t;
            return "error";
        }

        // Si hay otros operadores unarios, por ahora devolvemos el tipo del operando
        return t;
    }
}
