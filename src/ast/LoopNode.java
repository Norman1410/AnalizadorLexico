package ast;

import java.util.*;

public class LoopNode extends ASTNode {
    private ASTNode block;
    private ASTNode exitCondition;

    @Override
    public Map<String, Object> toJsonObject() {
        Map<String, Object> node = createNode("Loop");
        if (block != null)
            node.put("block", block.toJsonObject());
        if (exitCondition != null)
            node.put("exitCondition", exitCondition.toJsonObject());
        return node;
    }

    public LoopNode(ASTNode block, ASTNode exitCondition, int line, int column) {
        super(line, column);
        this.block = block;
        this.exitCondition = exitCondition;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("Loop");
        if (block != null)
            block.print(indent + 1);
        if (exitCondition != null) {
            printIndent(indent + 1);
            System.out.println("Exit When:");
            exitCondition.print(indent + 2);
        }
    }

    @Override
    public void validate(semantics.SymbolTable st) {
        if (st == null) return;

        // Scope propio del loop (así el exit when "ve" las variables declaradas en el body)
        st.enterScope("loop@" + getLine() + ":" + getColumn());

        BlockNode body = getBody();
        if (body != null && body.getStatements() != null) {
            for (ASTNode stmt : body.getStatements()) {
                if (stmt == null) continue;

                // Igual que BlockNode: declarar primero si es DeclNode
                if (stmt instanceof DeclNode d) {
                    st.declare(new semantics.SymbolInfo(
                            d.getName(),
                            d.getTypeName(),
                            semantics.SymbolKind.LOCAL_VAR,
                            d.getLine(),
                            d.getColumn(),
                            d.getDims()
                    ));

                    // Validar initializer si existe
                    ASTNode init = d.getInitializer();
                    if (init != null) init.validate(st);

                } else {
                    stmt.validate(st);
                }
            }
        }

        // Validar exit when dentro del mismo scope del loop
        if (exitCondition != null) {
            exitCondition.validate(st);
            String t = exitCondition.getType(st);
            if (t != null && !t.equals("boolean") && !t.equals("error") && !t.equals("unknown")) {
                st.addError("Exit when requiere condición boolean (line=" + (getLine() + 1) +
                        ", col=" + (getColumn() + 1) + ")");
            }
        }

        st.exitScope();
    }



    @Override
    public String getType(semantics.SymbolTable st) {
        return "void";
    }

    public BlockNode getBody() {
        return (block instanceof BlockNode) ? (BlockNode) block : null;
    }
}
