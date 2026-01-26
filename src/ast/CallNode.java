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
    public String getName() { return identifier; }
    public List<ASTNode> getArguments() { return arguments; }

}
