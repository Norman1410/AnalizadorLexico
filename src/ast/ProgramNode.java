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
        //System.out.println("=== DEBUG buildSymbolTable ===");
        //System.out.println("declarations = " + (declarations == null ? "null" : declarations.size()));
        //System.out.println("functions = " + (functions == null ? "null" : functions.size()));
        //System.out.println("mainBlock = " + (mainBlock == null ? "null" : mainBlock.getClass().getName()));

        // 1) Globales
        if (declarations != null) {
            for (ASTNode n : declarations) {
                if (n instanceof DeclNode d) {
                    //System.out.println("DECLARANDO GLOBAL: " + d.getName());

                    st.declare(new SymbolInfo(
                            d.getName(),
                            d.getTypeName(),          // <-- AQUÍ el cambio
                            SymbolKind.GLOBAL_VAR,
                            d.getLine(),
                            d.getColumn(),
                            d.getDims()
                    ));
                }
            }
        }

        // 2) Funciones (firma en global + scope propio)
        if (functions != null) {
            for (ASTNode fn : functions) {
                if (fn instanceof FunctionNode f) {

                    // firma en global
                    st.declare(new SymbolInfo(
                            f.getName(),
                            f.getReturnType(),
                            SymbolKind.FUNCTION,
                            f.getLine(),
                            f.getColumn(),
                            Collections.emptyList()
                    ));
                    //System.out.println("ENTER FUNC SCOPE: " + f.getName());
                    // scope de la función
                    st.enterScope(f.getName());
                    // "tipo" dentro del scope de la función (para imprimir como el profe)
                    st.declare(new SymbolInfo(
                            "tipo",
                            "function:" + f.getReturnType(),
                            SymbolKind.META,
                            f.getLine(),
                            f.getColumn(),
                            Collections.emptyList()
                    ));

                    // params
                    for (ParamNode p : f.getParams()) {
                        st.declare(new SymbolInfo(
                                p.getName(),
                                p.getType(),
                                SymbolKind.PARAM,
                                p.getLine(),
                                p.getColumn(),
                                Collections.emptyList()
                        ));
                    }

                    // locals del bloque
                    BlockNode fb = f.getBlock();
                    if (fb != null) {
                        collectLocalsFromBlock(fb, st);
                    }

                    // ✅ IMPORTANTÍSIMO: cerrar scope de la función
                    st.exitScope();
                }
            }
        }

        // 3) Main (scope main)
        st.enterScope("main");
        // "tipo" dentro del scope de main (si tu main no tiene tipo, pon "main:void")
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
                Collections.emptyList()
        ));


        if (mainBlock instanceof MainNode mn) {
            collectLocalsFromBlock(mn.getBlock(), st);
        }
        st.exitScope();
        //System.out.println("DEBUG lookup x = " + st.lookup("x"));
        //System.out.println("DEBUG lookup mi = " + st.lookup("mi"));
        return st;
    }

    /** Ajusta según tu estructura real: aquí busco DeclNode dentro del BlockNode */
    private void collectLocalsFromBlock(BlockNode b, SymbolTable st) {
        if (b == null) return;
        List<ASTNode> stmts = b.getStatements(); // <-- ajusta nombre
        if (stmts == null) return;

        for (ASTNode s : stmts) {
            if (s instanceof DeclNode d && !d.isGlobal()) {
                st.declare(new SymbolInfo(
                        d.getName(),
                        d.getTypeName(),
                        SymbolKind.LOCAL_VAR,
                        d.getLine(),
                        d.getColumn(),
                        d.getDims()
                ));
            }

            // Si tienes bloques anidados (loop/for/decide), puedes irlos agregando:
            if (s instanceof BlockNode bb) {
                collectLocalsFromBlock(bb, st);
                continue;
            }

            if (s instanceof LoopNode ln) {
                BlockNode body = ln.getBody();
                if (body != null) collectLocalsFromBlock(body, st);
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
                            d2.getDims()
                    ));
                }

                BlockNode body = fn.getBody();
                if (body != null) collectLocalsFromBlock(body, st);
                continue;
            }

            if (s instanceof DecideNode dn) {
                for (CaseNode cn : dn.getCases()) {
                    if (cn.getBlock() != null) collectLocalsFromBlock(cn.getBlock(), st);
                }
                if (dn.getElseBlock() != null) collectLocalsFromBlock(dn.getElseBlock(), st);
            }
        }
    }

}