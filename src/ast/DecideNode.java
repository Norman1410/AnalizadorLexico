package ast;

import java.util.*;

public class DecideNode extends ASTNode {
    private ASTNode expression;
    private List<ASTNode> cases;
    private ASTNode elseBlock;

    public DecideNode(ASTNode expression, List<ASTNode> cases, ASTNode elseBlock, int line, int column) {
        super(line, column);
        this.expression = expression;
        this.cases = cases;
        this.elseBlock = elseBlock;
    }

    @Override
    public Map<String, Object> toJsonObject() {
        Map<String, Object> node = createNode("Decide");
        if (expression != null)
            node.put("condition", expression.toJsonObject());
        List<Map<String, Object>> casesJson = new ArrayList<>();
        if (cases != null) {
            for (ASTNode c : cases)
                casesJson.add(c.toJsonObject());
        }
        node.put("cases", casesJson);
        if (elseBlock != null)
            node.put("elseBlock", elseBlock.toJsonObject());
        return node;
    }

    @Override
    public void print(int indent) {
        // ...
        printIndent(indent);
        System.out.println("Decide");
        if (expression != null) {
            printIndent(indent + 1);
            System.out.println("Condition:");
            expression.print(indent + 2);
        }
        if (cases != null) {
            for (ASTNode c : cases)
                c.print(indent + 1);
        }
        if (elseBlock != null) {
            printIndent(indent + 1);
            System.out.println("Else:");
            elseBlock.print(indent + 2);
        }
    }
    public List<CaseNode> getCases() {
        if (cases == null) return Collections.emptyList();
        List<CaseNode> out = new ArrayList<>();
        for (ASTNode n : cases) {
            if (n instanceof CaseNode cn) out.add(cn);
        }
        return out;
    }

    public BlockNode getElseBlock() {
        return (elseBlock instanceof BlockNode) ? (BlockNode) elseBlock : null;
    }

}
