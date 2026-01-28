package ast;

import java.util.*;

public class MainNode extends ASTNode {
    private ASTNode block;
    private boolean isCoal;
    private String name = "navidad";

    public MainNode(ASTNode block, boolean isCoal, int line, int column) {
        super(line, column);
        this.block = block;
        this.isCoal = isCoal;
    }

    @Override
    public Map<String, Object> toJsonObject() {
        Map<String, Object> node = createNode("Main");
        node.put("name", name);
        node.put("isCoal", isCoal);
        if (block != null)
            node.put("block", block.toJsonObject());
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
        if (st == null) return;

        st.enterScope("main");
        st.declare(new semantics.SymbolInfo(
                "tipo",
                "main:void",
                semantics.SymbolKind.META,
                getLine(),
                getColumn(),
                java.util.Collections.emptyList()
        ));

        if (block != null) block.validate(st);

        st.exitScope();
    }


    @Override
    public String getType(semantics.SymbolTable st) {
        return "void";
    }
}
