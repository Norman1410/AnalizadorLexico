package ast;

import java.util.*;

public class BlockNode extends ASTNode {
    private List<ASTNode> statements;

    public BlockNode(List<ASTNode> statements, int line, int column) {
        super(line, column);
        this.statements = statements;
    }

    @Override
    public Map<String, Object> toJsonObject() {
        Map<String, Object> node = createNode("Block");
        List<Map<String, Object>> stmtsJson = new ArrayList<>();
        if (statements != null) {
            for (ASTNode s : statements) {
                if (s != null)
                    stmtsJson.add(s.toJsonObject());
            }
        }
        node.put("statements", stmtsJson);
        return node;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("Block");
        if (statements != null) {
            for (ASTNode s : statements) {
                if (s != null)
                    s.print(indent + 1);
            }
        }
    }

    @Override
    public void validate(semantics.SymbolTable st) {
    }

    @Override
    public String getType(semantics.SymbolTable st) {
        return "void";
    }

    public List<ASTNode> getStatements() {
        return statements;
    }
}
