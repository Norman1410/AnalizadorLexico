package ast;

import java.util.*;

public class ProgramNode extends ASTNode {
    private List<ASTNode> declarations;
    private List<ASTNode> functions;
    private ASTNode mainBlock;

    @Override
    public Map<String, Object> toJsonObject() {
        Map<String, Object> node = createNode("Program");

        List<Map<String, Object>> declsJson = new ArrayList<>();
        if (declarations != null) {
            for (ASTNode d : declarations)
                declsJson.add(d.toJsonObject());
        }
        node.put("declarations", declsJson);

        List<Map<String, Object>> funcsJson = new ArrayList<>();
        if (functions != null) {
            for (ASTNode f : functions)
                funcsJson.add(f.toJsonObject());
        }
        node.put("functions", funcsJson);

        if (mainBlock != null) {
            node.put("main", mainBlock.toJsonObject());
        }
        return node;
    }

    public ProgramNode(List<ASTNode> declarations, List<ASTNode> functions, ASTNode mainBlock, int line, int column) {
        super(line, column);
        this.declarations = declarations;
        this.functions = functions;
        this.mainBlock = mainBlock;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("Program");

        if (declarations != null) {
            for (ASTNode d : declarations)
                d.print(indent + 1);
        }
        if (functions != null) {
            for (ASTNode f : functions)
                f.print(indent + 1);
        }
        if (mainBlock != null) {
            mainBlock.print(indent + 1);
        }
    }

    @Override
    public void validate(semantics.SymbolTable st) {
        if (declarations != null) {
            for (ASTNode d : declarations)
                d.validate(st);
        }
        if (functions != null) {
            for (ASTNode f : functions)
                f.validate(st);
        }
        if (mainBlock != null) {
            mainBlock.validate(st);
        }
    }

    @Override
    public String getType(semantics.SymbolTable st) {
        return "void";
    }

    public semantics.SymbolTable buildSymbolTable() {
        return new semantics.SymbolTable();
    }

    public void validateArraySemantics(semantics.SymbolTable st) {
    }
}