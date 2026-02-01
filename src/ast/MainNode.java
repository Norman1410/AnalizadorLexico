package ast;

import java.util.*;

public class MainNode extends ASTNode {
    private ASTNode block;
    private boolean isCoal;
    private String name = "navidad";

    private List<ParamNode> parameters;

    public MainNode(ASTNode block, boolean isCoal, List<ParamNode> parameters, int line, int column) {
        super(line, column);
        this.block = block;
        this.isCoal = isCoal;
        this.parameters = parameters;
    }

    @Override
    public Map<String, Object> toJsonObject() {
        Map<String, Object> node = createNode("Main");
        node.put("name", name);
        node.put("isCoal", isCoal);
        if (block != null)
            node.put("block", block.toJsonObject());
        if (parameters != null) {
            List<Map<String, Object>> paramsJson = new ArrayList<>();
            for (ParamNode p : parameters)
                paramsJson.add(p.toJsonObject());
            node.put("parameters", paramsJson);
        }
        return node;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("Main (" + (isCoal ? "coal" : "") + " " + name + ")");
        if (block != null)
            block.print(indent + 1);
    }

    @Override
    public void validate(semantics.SymbolTable st) {
        if (st == null)
            return;

        if (parameters != null && !parameters.isEmpty()) {
            st.addError("Error semántico (línea " + (getLine() + 1) + ", col " + (getColumn() + 1) + "): " +
                    "El bloque principal 'navidad' no debe tener parámetros.");
        }

        st.enterScope("main");

        st.declare(new semantics.SymbolInfo(
                "tipo",
                "main:" + (isCoal ? "int" : "void"),
                semantics.SymbolKind.META,
                getLine(),
                getColumn(),
                java.util.Collections.emptyList()));

        BlockNode b = getBlock();
        if (b != null)
            b.validate(st);

        st.exitScope();
    }

    @Override
    public String getType(semantics.SymbolTable st) {
        return isCoal ? "int" : "void";
    }

    public BlockNode getBlock() {
        return (block instanceof BlockNode) ? (BlockNode) block : null;
    }

    public boolean isCoal() {
        return isCoal;
    }
}
