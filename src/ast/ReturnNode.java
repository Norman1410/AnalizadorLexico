package ast;

import java.util.*;

public class ReturnNode extends ASTNode {
    private ASTNode expression;

    public ReturnNode(ASTNode expression, int line, int column) {
        super(line, column);
        this.expression = expression;
    }

    @Override
    public Map<String, Object> toJsonObject() {
        Map<String, Object> node = createNode("Return");
        if (expression != null)
            node.put("expression", expression.toJsonObject());
        return node;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("Return");
        if (expression != null)
            expression.print(indent + 1);
    }

    @Override
    public void validate(semantics.SymbolTable st) {
        if (st == null)
            return;

        if (expression != null)
            expression.validate(st);

        String expectedType = st.getCurrentExpectedReturnType();

        if (expectedType == null) {
            st.addError("Error semántico (línea " + (getLine() + 1) + ", col " + (getColumn() + 1) + "): " +
                    "'return' solo puede usarse dentro de una función.");
            return;
        }

        if ("main".equals(st.currentScope()) && "void".equals(expectedType)) {
            st.addError("Error semántico (línea " + (getLine() + 1) + ", col " + (getColumn() + 1) + "): " +
                    "No se permite 'return' dentro del bloque principal 'navidad' a menos que sea de tipo coal.");
            return;
        }
        String actualType = (expression == null) ? "void" : expression.getType(st);

        if ("error".equals(actualType) || "unknown".equals(actualType))
            return;

        if (!isCompatible(expectedType, actualType)) {
            st.addError("Error semántico (línea " + (getLine() + 1) + ", col " + (getColumn() + 1) + "): " +
                    "Tipo de retorno incompatible. Se esperaba '" + expectedType + "' pero se obtuvo '" + actualType
                    + "'.");
        }
    }

    @Override
    public String getType(semantics.SymbolTable st) {
        return "void";
    }

    public ASTNode getExpression() {
        return expression;
    }

}
