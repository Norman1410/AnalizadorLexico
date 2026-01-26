package ast;

import java.util.*;
import semantics.*;

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

    public SymbolTable buildSymbolTable() {
        SymbolTable st = new SymbolTable();

        if (declarations != null) {
            for (ASTNode n : declarations) {
                if (n instanceof DeclNode d) {
                    st.declare(new SymbolInfo(
                            d.getName(),
                            d.getTypeName(),
                            SymbolKind.GLOBAL_VAR,
                            d.getLine(),
                            d.getColumn(),
                            d.getDims()));
                }
            }
        }

        if (functions != null) {
            for (ASTNode fn : functions) {
                if (fn instanceof FunctionNode f) {

                    st.declare(new SymbolInfo(
                            f.getName(),
                            f.getReturnType(),
                            SymbolKind.FUNCTION,
                            f.getLine(),
                            f.getColumn(),
                            Collections.emptyList()));
                    st.enterScope(f.getName());
                    st.declare(new SymbolInfo(
                            "tipo",
                            "function:" + f.getReturnType(),
                            SymbolKind.META,
                            f.getLine(),
                            f.getColumn(),
                            Collections.emptyList()));

                    for (ParamNode p : f.getParams()) {
                        st.declare(new SymbolInfo(
                                p.getName(),
                                p.getType(),
                                SymbolKind.PARAM,
                                p.getLine(),
                                p.getColumn(),
                                Collections.emptyList()));
                    }

                    BlockNode fb = f.getBlock();
                    if (fb != null) {
                        collectLocalsFromBlock(fb, st);
                    }

                    st.exitScope();
                }
            }
        }

        st.enterScope("main");
        int ml = 0, mc = 0;
        if (mainBlock instanceof MainNode mn2) {
            ml = mn2.getLine();
            mc = mn2.getColumn();
        }
        st.declare(new SymbolInfo(
                "tipo",
                "main:void",
                SymbolKind.META,
                ml,
                mc,
                Collections.emptyList()));

        if (mainBlock instanceof MainNode mn) {
            collectLocalsFromBlock(mn.getBlock(), st);
        }
        st.exitScope();
        return st;
    }

    private void collectLocalsFromBlock(BlockNode b, SymbolTable st) {
        if (b == null)
            return;
        List<ASTNode> stmts = b.getStatements();
        if (stmts == null)
            return;

        for (ASTNode s : stmts) {
            if (s instanceof DeclNode d && !d.isGlobal()) {
                st.declare(new SymbolInfo(
                        d.getName(),
                        d.getTypeName(),
                        SymbolKind.LOCAL_VAR,
                        d.getLine(),
                        d.getColumn(),
                        d.getDims()));
            }

            if (s instanceof BlockNode bb) {
                collectLocalsFromBlock(bb, st);
                continue;
            }

            if (s instanceof LoopNode ln) {
                BlockNode body = ln.getBody();
                if (body != null)
                    collectLocalsFromBlock(body, st);
                continue;
            }

            if (s instanceof ForNode fn) {

                ASTNode init = fn.getInit();
                if (init instanceof DeclNode d2) {
                    st.declare(new SymbolInfo(
                            d2.getName(),
                            d2.getTypeName(),
                            SymbolKind.LOCAL_VAR,
                            d2.getLine(),
                            d2.getColumn(),
                            d2.getDims()));
                }

                BlockNode body = fn.getBody();
                if (body != null)
                    collectLocalsFromBlock(body, st);
                continue;
            }

            if (s instanceof DecideNode dn) {
                for (CaseNode cn : dn.getCases()) {
                    if (cn.getBlock() != null)
                        collectLocalsFromBlock(cn.getBlock(), st);
                }
                if (dn.getElseBlock() != null)
                    collectLocalsFromBlock(dn.getElseBlock(), st);
            }
        }
    }

    public void validateArraySemantics(SymbolTable st) {
        if (st == null) return;

        // Recorremos TODO el árbol (declaraciones, funciones, main)
        walk(this, st, new IdentityHashMap<>());
    }

    private void walk(Object obj, SymbolTable st, IdentityHashMap<Object, Boolean> seen) {
        if (obj == null) return;
        if (seen.put(obj, Boolean.TRUE) != null) return;

        // Reglas específicas
        if (obj instanceof AssignNode an) {
            checkAssign(an, st);
        } else if (obj instanceof CallNode cn) {
            checkCall(cn, st);
        }

        // Recorremos hijos por reflexión (ASTNodes y listas)
        try {
            Class<?> c = obj.getClass();
            while (c != null) {
                for (var f : c.getDeclaredFields()) {
                    f.setAccessible(true);
                    Object v = f.get(obj);

                    if (v instanceof ASTNode) {
                        walk(v, st, seen);
                    } else if (v instanceof List<?> list) {
                        for (Object it : list) {
                            if (it instanceof ASTNode) walk(it, st, seen);
                        }
                    }
                }
                c = c.getSuperclass();
            }
        } catch (Exception ignored) {}
    }

    private void checkAssign(AssignNode an, SymbolTable st) {

        // Caso: "reemplazar arreglo" (asignar a un arreglo completo algo que NO es arreglo)
        ASTNode target = an.getTarget();
        ASTNode rhs    = an.getExpression();

        // Solo nos importa cuando el target es una VARIABLE COMPLETA: arr = ...
        if (target instanceof VariableNode) {

            VariableNode vn = (VariableNode) target;
            SymbolInfo si = st.lookup(vn.getName());

            // Si la variable destino es un arreglo
            if (si != null && si.dims != null && !si.dims.isEmpty()) {

                boolean rhsEsArreglo = false;

                // 1) Literal de arreglo: { ... }
                if (rhs instanceof ArrayLiteralNode) {
                    rhsEsArreglo = true;
                }
                // 2) Variable que también sea arreglo: arr2
                else if (rhs instanceof VariableNode) {
                    VariableNode rv = (VariableNode) rhs;
                    SymbolInfo rsi = st.lookup(rv.getName());

                    if (rsi != null && rsi.dims != null && !rsi.dims.isEmpty()) {
                        rhsEsArreglo = true;
                    }
                }

                // Si NO es arreglo, error semántico
                if (!rhsEsArreglo) {
                    st.addError(
                            "Asignación inválida: no se puede asignar un valor escalar al arreglo '" +
                                    vn.getName() +
                                    "' (línea=" + (an.getLine() + 1) +
                                    ", columna=" + (an.getColumn() + 1) + ")"
                    );
                }
            }
        }
    }


    private void checkCall(CallNode cn, SymbolTable st) {
        // Caso: "error por permitirlo como argumento"
        // - prohibir { ... } como argumento
        // - prohibir pasar una VARIABLE que sea arreglo como argumento (arr)
        var args = cn.getArguments();
        if (args == null) return;

        for (ASTNode a : args) {
            if (a instanceof ArrayLiteralNode) {
                st.addError("Argumento inválido: no se permite pasar un literal de arreglo como argumento en '" +
                        cn.getName() + "' (line=" + (cn.getLine()+1) + ", col=" + (cn.getColumn()+1) + ")");
            }
            if (a instanceof VariableNode vn) {
                var si = st.lookup(vn.getName());
                if (si != null && si.dims != null && !si.dims.isEmpty()) {
                    st.addError("Argumento inválido: no se permite pasar el arreglo completo '" + vn.getName() +
                            "' como argumento en '" + cn.getName() + "' (line=" + (cn.getLine()+1) + ", col=" + (cn.getColumn()+1) + ")");
                }
            }
            // ArrayAccessNode (a[i]) sí se permite, porque es un elemento
        }
    }


}