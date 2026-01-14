package ast;

import java.util.*;

/**
 * Clase base para todos los nodos del Árbol de Sintaxis Abstracta (AST).
 */
public abstract class ASTNode {
    protected int line;
    protected int column;

    public ASTNode(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    /**
     * Método para imprimir el nodo de forma jerárquica con indentación.
     */
    public abstract void print(int indent);

    /**
     * Convierte el nodo a una estructura de Map para exportación JSON.
     */
    public abstract Map<String, Object> toJsonObject();

    /**
     * Utilidad para crear el Map base de un nodo JSON.
     */
    protected Map<String, Object> createNode(String type) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("type", type);
        node.put("line", line + 1);
        node.put("column", column + 1);
        return node;
    }

    /**
     * Utilidad para imprimir espacios de indentación.
     */
    protected void printIndent(int indent) {
        for (int i = 0; i < indent; i++) {
            System.out.print("  ");
        }
    }
}
