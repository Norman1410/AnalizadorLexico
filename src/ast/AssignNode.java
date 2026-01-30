package ast;

import java.util.*;

public class AssignNode extends ASTNode {
    private ASTNode target;;
    private ASTNode expression;

    public AssignNode(ASTNode target, ASTNode expression, int line, int column) {
        super(line, column);
        this.target = target;
        this.expression = expression;
    }

    @Override
    public Map<String, Object> toJsonObject() {
        Map<String, Object> node = createNode("Assignment");
        node.put("target", target.toJsonObject());
        if (expression != null)
            node.put("expression", expression.toJsonObject());
        return node;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("Assignment:");
        target.print(indent + 1);
        printIndent(indent + 1);
        System.out.println("->");
        if (expression != null)
            expression.print(indent + 1);
    }

    @Override
    public void validate(semantics.SymbolTable st) {
        if (st == null)
            return;

        if (target != null)
            target.validate(st);
        if (expression != null)
            expression.validate(st);

        // Ensure target is an lvalue
        if (!(target instanceof VariableNode) && !(target instanceof ArrayAccessNode)) {
            st.addError("Error semántico (línea " + (getLine() + 1) + ", col " + (getColumn() + 1) + "): " +
                    "La parte izquierda de una asignación debe ser un lvalue (variable o acceso a arreglo).");
            return;
        }

        String targetType = (target == null) ? "error" : target.getType(st);
        String exprType = (expression == null) ? "error" : expression.getType(st);

        if ("error".equals(targetType) || "error".equals(exprType))
            return;
        if ("unknown".equals(targetType) || "unknown".equals(exprType))
            return;

        if (target instanceof VariableNode) {
            VariableNode vn = (VariableNode) target;
            semantics.SymbolInfo info = st.lookup(vn.getName());
            if (info != null && !info.dims.isEmpty() && !(expression instanceof ArrayLiteralNode)) {
                st.addError("Error semántico (línea " + (getLine() + 1) + ", col " + (getColumn() + 1) + "): " +
                        "No se puede asignar un valor escalar a un arreglo '" + vn.getName() + "'.");
                return;
            }
        }

        if (!isCompatible(targetType, exprType)) {
            st.addError("Error semántico (línea " + (getLine() + 1) + ", col " + (getColumn() + 1) + "): " +
                    "Tipo incompatible en asignación. No se puede asignar '" + exprType + "' a un '" + targetType
                    + "'.");
        }
    }

    @Override
    public String getType(semantics.SymbolTable st) {
        return "void";
    }

    public ASTNode getTarget() {
        return target;
    }

    public ASTNode getExpression() {
        return expression;
    }
}
