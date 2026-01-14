package ast;

import java.util.*;

public class FunctionNode extends ASTNode {
    private String name;
    private String returnType;
    private List<ParamNode> parameters;
    private ASTNode block;

    public FunctionNode(String name, String returnType, List<ParamNode> parameters, ASTNode block, int line,
            int column) {
        super(line, column);
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
        this.block = block;
    }

    @Override
    public Map<String, Object> toJsonObject() {
        Map<String, Object> node = createNode("Function");
        node.put("name", name);
        node.put("returnType", returnType);
        List<Map<String, Object>> paramsJson = new ArrayList<>();
        if (parameters != null) {
            for (ParamNode p : parameters) {
                paramsJson.add(p.toJsonObject());
            }
        }
        node.put("parameters", paramsJson);
        if (block != null)
            node.put("block", block.toJsonObject());
        return node;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.print("Function: " + name + " (");
        if (parameters != null) {
            for (int i = 0; i < parameters.size(); i++) {
                ParamNode p = parameters.get(i);
                System.out.print(p.getName() + ": " + p.getType());
                if (i < parameters.size() - 1)
                    System.out.print(", ");
            }
        }
        System.out.println(") -> " + returnType);
        if (block != null)
            block.print(indent + 1);
    }
}
