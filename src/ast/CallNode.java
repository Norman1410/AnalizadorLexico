package ast;

import java.util.*;

public class CallNode extends ASTNode {
    private String identifier;
    private List<ASTNode> arguments;

    public CallNode(String identifier, List<ASTNode> arguments, int line, int column) {
        super(line, column);
        this.identifier = identifier;
        this.arguments = arguments;
    }

    @Override
    public Map<String, Object> toJsonObject() {
        Map<String, Object> node = createNode("Call");
        node.put("functionName", identifier);
        List<Map<String, Object>> argsJson = new ArrayList<>();
        if (arguments != null) {
            for (ASTNode a : arguments)
                argsJson.add(a.toJsonObject());
        }
        node.put("arguments", argsJson);
        return node;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("Function Call: " + identifier);
        if (arguments != null) {
            for (ASTNode arg : arguments)
                arg.print(indent + 1);
        }
    }

    @Override
    public void validate(semantics.SymbolTable st) {
        if (st == null)
            return;

        if (arguments != null) {
            for (ASTNode arg : arguments)
                arg.validate(st);
        }

        semantics.SymbolInfo info = st.lookup(identifier);
        if (info == null) {
            st.addError("Error semántico (línea " + (getLine() + 1) + ", col " + (getColumn() + 1) + "): " +
                    "La función '" + identifier + "' no ha sido declarada.");
            return;
        }

        if (info.kind != semantics.SymbolKind.FUNCTION) {
            st.addError("Error semántico (línea " + (getLine() + 1) + ", col " + (getColumn() + 1) + "): " +
                    "'" + identifier + "' no es una función.");
            return;
        }

        int paramCount = (info.paramTypes == null) ? 0 : info.paramTypes.size();
        int argCount = (arguments == null) ? 0 : arguments.size();

        if (argCount != paramCount) {
            st.addError("Error semántico (línea " + (getLine() + 1) + ", col " + (getColumn() + 1) + "): " +
                    "Cantidad de argumentos incorrecta para '" + identifier + "'. Se esperaban " + paramCount
                    + " pero se enviaron " + argCount + ".");
            return;
        }

        if (arguments != null) {
            for (int i = 0; i < argCount; i++) {
                String argType = arguments.get(i).getType(st);
                String paramType = info.paramTypes.get(i);

                if ("error".equals(argType) || "unknown".equals(argType))
                    continue;

                if (!isCompatible(paramType, argType)) {
                    st.addError("Error semántico (línea " + (arguments.get(i).getLine() + 1) + ", col "
                            + (arguments.get(i).getColumn() + 1) + "): " +
                            "Tipo de argumento incompatible en la posición " + (i + 1) + " para '" + identifier
                            + "'. Se esperaba '" + paramType + "' pero se obtuvo '" + argType + "'.");
                }
            }
        }
    }

    @Override
    public String getType(semantics.SymbolTable st) {
        semantics.SymbolInfo info = st.lookup(identifier);
        if (info != null && info.kind == semantics.SymbolKind.FUNCTION) {
            return info.type;
        }
        return "error";
    }

    public String getName() {
        return identifier;
    }

    public List<ASTNode> getArguments() {
        return arguments;
    }
}
