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

    @Override
    public void validate(semantics.SymbolTable st) {
        if (st == null)
            return;

        if (expression != null) {
            expression.validate(st);
            String t = expression.getType(st);
            if (!"error".equals(t) && !"unknown".equals(t) && !"boolean".equals(t)) {
                st.addError("Error semántico (línea " + (getLine() + 1) + ", col " + (getColumn() + 1) + "): " +
                        "La condición de 'decide' debe ser boolean, pero se obtuvo '" + t + "'.");
            }
        }

        if (cases != null) {
            for (ASTNode c : cases) {
                if (c != null)
                    c.validate(st);
            }
        }

        if (elseBlock != null) {
            elseBlock.validate(st);
        }
    }

    @Override
    public String getType(semantics.SymbolTable st) {
        return "void";
    }

    public List<CaseNode> getCases() {
        if (cases == null)
            return Collections.emptyList();
        List<CaseNode> out = new ArrayList<>();
        for (ASTNode n : cases) {
            if (n instanceof CaseNode)
                out.add((CaseNode) n);
        }
        return out;
    }

    public ASTNode getElseBlock() {
        return elseBlock;
    }
}
