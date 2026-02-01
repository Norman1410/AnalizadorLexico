package ast;

import java.util.*;

public class ProgramNode extends ASTNode {
    private List<ASTNode> declarations;
    private List<ASTNode> functions;
    private List<MainNode> mainBlocks;

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

        List<Map<String, Object>> mainsJson = new ArrayList<>();
        if (mainBlocks != null) {
            for (MainNode m : mainBlocks)
                mainsJson.add(m.toJsonObject());
        }
        node.put("mains", mainsJson);
        return node;
    }

    public ProgramNode(List<ASTNode> declarations, List<ASTNode> functions, List<MainNode> mainBlocks, int line,
            int column) {
        super(line, column);
        this.declarations = declarations;
        this.functions = functions;
        this.mainBlocks = mainBlocks;
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
        if (mainBlocks != null) {
            for (MainNode m : mainBlocks)
                m.print(indent + 1);
        }
    }

    @Override
    public void validate(semantics.SymbolTable st) {
        if (st == null)
            return;

        // 1) Globales
        if (declarations != null) {
            for (ASTNode d : declarations) {
                if (d instanceof DeclNode) {
                    DeclNode dn = (DeclNode) d;
                    st.declare(new semantics.SymbolInfo(
                            dn.getName(),
                            dn.getTypeName(),
                            semantics.SymbolKind.GLOBAL_VAR,
                            dn.getLine(),
                            dn.getColumn(),
                            dn.getDims()));

                    ASTNode init = dn.getInitializer();
                    if (init != null)
                        init.validate(st);
                }
            }
        }

        // 2) Firmas de funciones en global (para CallNode)
        if (functions != null) {
            for (ASTNode f : functions) {
                if (f instanceof FunctionNode) {
                    FunctionNode fn = (FunctionNode) f;
                    List<String> pTypes = new ArrayList<>();
                    if (fn.getParams() != null) {
                        for (ParamNode p : fn.getParams()) {
                            if (p != null)
                                pTypes.add(p.getType());
                        }
                    }

                    st.declare(new semantics.SymbolInfo(
                            fn.getName(),
                            fn.getReturnType(),
                            semantics.SymbolKind.FUNCTION,
                            fn.getLine(),
                            fn.getColumn(),
                            java.util.Collections.emptyList(),
                            pTypes));
                }
            }
        }

        // 3) Validar funciones (ya abren su propio scope)
        if (functions != null) {
            for (ASTNode f : functions) {
                if (f instanceof FunctionNode) {
                    FunctionNode fn = (FunctionNode) f;
                    fn.validate(st);
                }
            }
        }

        // 4) Validar main (abre scope main)
        if (mainBlocks != null && !mainBlocks.isEmpty()) {
            if (mainBlocks.size() > 1) {
                st.addError("Error semántico: Se detectaron múltiples bloques 'navidad'. Solo se permite uno.");
            }
            for (MainNode m : mainBlocks) {
                m.validate(st);
            }
        } else {
            st.addError("Error semántico: No se encontró el bloque principal 'navidad'.");
        }
    }

    @Override
    public String getType(semantics.SymbolTable st) {
        return "void";
    }

    public semantics.SymbolTable buildSymbolTable() {
        semantics.SymbolTable st = new semantics.SymbolTable();
        st.enterScope("global");

        // 1) Globales
        if (declarations != null) {
            for (ASTNode n : declarations) {
                if (n instanceof DeclNode) {
                    DeclNode d = (DeclNode) n;
                    st.declare(new semantics.SymbolInfo(
                            d.getName(),
                            d.getTypeName(),
                            semantics.SymbolKind.GLOBAL_VAR,
                            d.getLine(),
                            d.getColumn(),
                            d.getDims()));
                }
            }
        }

        // 2) Funciones
        if (functions != null) {
            for (ASTNode fn : functions) {
                if (!(fn instanceof FunctionNode))
                    continue;

                Object f = fn;

                String fname = callString(f, "getName");
                if (fname == null)
                    fname = "function@" + fn.getLine() + ":" + fn.getColumn();

                String rtype = firstNonNull(
                        callString(f, "getReturnType"),
                        callString(f, "getTypeName"),
                        callString(f, "getType"),
                        getStringField(f, "returnType"),
                        getStringField(f, "type"),
                        getStringField(f, "typeName"));
                if (rtype == null)
                    rtype = "void";

                // declarar firma en global
                st.declare(new semantics.SymbolInfo(
                        fname,
                        rtype,
                        semantics.SymbolKind.FUNCTION,
                        fn.getLine(),
                        fn.getColumn(),
                        java.util.Collections.emptyList()));

                // scope de función
                st.enterScope(fname);

                st.declare(new semantics.SymbolInfo(
                        "tipo",
                        "function:" + rtype,
                        semantics.SymbolKind.META,
                        fn.getLine(),
                        fn.getColumn(),
                        java.util.Collections.emptyList()));

                // parámetros
                List<?> params = firstNonNullList(
                        callList(f, "getParams"),
                        callList(f, "getParameters"),
                        callList(f, "getParamList"),
                        getListField(f, "params"),
                        getListField(f, "parameters"));

                if (params != null) {
                    for (Object p : params) {
                        if (!(p instanceof ParamNode))
                            continue;
                        ParamNode pn = (ParamNode) p;
                        st.declare(new semantics.SymbolInfo(
                                pn.getName(),
                                pn.getType(),
                                semantics.SymbolKind.PARAM,
                                pn.getLine(),
                                pn.getColumn(),
                                java.util.Collections.emptyList()));
                    }
                }

                // bloque de función
                BlockNode fb = firstNonNullBlock(
                        (BlockNode) callObject(f, "getBlock"),
                        (BlockNode) callObject(f, "getBody"),
                        (BlockNode) getObjectField(f, "block"),
                        (BlockNode) getObjectField(f, "body"));

                if (fb != null) {
                    collectLocalsFromBlock(fb, st);
                }

                st.exitScope();
            }
        }

        st.enterScope("main");

        MainNode primaryMain = (mainBlocks != null && !mainBlocks.isEmpty()) ? mainBlocks.get(0) : null;

        st.declare(new semantics.SymbolInfo(
                "tipo",
                "main:void",
                semantics.SymbolKind.META,
                (primaryMain != null ? primaryMain.getLine() : 0),
                (primaryMain != null ? primaryMain.getColumn() : 0),
                java.util.Collections.emptyList()));

        BlockNode mb = null;

        if (primaryMain != null) {
            mb = primaryMain.getBlock();
        }

        if (mb == null && primaryMain != null) {
            mb = firstNonNullBlock(
                    (BlockNode) callObject(primaryMain, "getBlock"),
                    (BlockNode) getObjectField(primaryMain, "block"),
                    (BlockNode) getObjectField(primaryMain, "body"));
        }

        // Recolectar UNA sola vez
        if (mb != null) {
            collectLocalsFromBlock(mb, st);
        }

        st.exitScope(); // cierra main

        return st;

    }

    private void collectLocalsFromBlock(BlockNode b, semantics.SymbolTable st) {
        if (b == null || st == null)
            return;
        st.enterScope("block@" + b.getLine() + ":" + b.getColumn());

        for (ASTNode stmt : b.getStatements()) {

            // Declaraciones locales
            if (stmt instanceof DeclNode) {
                DeclNode d = (DeclNode) stmt;
                st.declare(new semantics.SymbolInfo(
                        d.getName(),
                        d.getTypeName(),
                        semantics.SymbolKind.LOCAL_VAR,
                        d.getLine(),
                        d.getColumn(),
                        d.getDims()));
            }

            // Bloque anidado
            else if (stmt instanceof BlockNode) {
                BlockNode bb = (BlockNode) stmt;
                collectLocalsFromBlock(bb, st);
            }

            // Decide
            else if (stmt instanceof DecideNode) {
                DecideNode dn = (DecideNode) stmt;
                for (ASTNode c : dn.getCases()) {
                    if (c instanceof CaseNode) {
                        CaseNode cn = (CaseNode) c;
                        collectLocalsFromBlock(cn.getBlock(), st);
                    }
                }
                if (dn.getElseBlock() instanceof BlockNode) {
                    BlockNode eb = (BlockNode) dn.getElseBlock();
                    collectLocalsFromBlock(eb, st);
                }
            }

            // Loop
            else if (stmt instanceof LoopNode) {
                LoopNode ln = (LoopNode) stmt;
                collectLocalsFromBlock(ln.getBody(), st);
            }

            // For
            else if (stmt instanceof ForNode) {
                ForNode fn = (ForNode) stmt;

                // Scope propio del for
                st.enterScope("for@" + fn.getLine() + ":" + fn.getColumn());

                if (fn.getInit() instanceof DeclNode) {
                    DeclNode d = (DeclNode) fn.getInit();
                    st.declare(new semantics.SymbolInfo(
                            d.getName(),
                            d.getTypeName(),
                            semantics.SymbolKind.LOCAL_VAR,
                            d.getLine(),
                            d.getColumn(),
                            d.getDims()));
                }

                collectLocalsFromBlock(fn.getBody(), st);

                st.exitScope();
            }
        }

        // CERRAR scope del bloque
        st.exitScope();
    }

    private static String firstNonNull(String... xs) {
        if (xs == null)
            return null;
        for (String x : xs)
            if (x != null)
                return x;
        return null;
    }

    private static List<?> firstNonNullList(List<?>... xs) {
        if (xs == null)
            return null;
        for (List<?> x : xs)
            if (x != null)
                return x;
        return null;
    }

    private static BlockNode firstNonNullBlock(BlockNode... xs) {
        if (xs == null)
            return null;
        for (BlockNode x : xs)
            if (x != null)
                return x;
        return null;
    }

    private static String callString(Object obj, String method) {
        Object r = callObject(obj, method);
        return (r instanceof String) ? (String) r : null;
    }

    @SuppressWarnings("unchecked")
    private static List<?> callList(Object obj, String method) {
        Object r = callObject(obj, method);
        return (r instanceof List<?>) ? (List<?>) r : null;
    }

    private static Object callObject(Object obj, String method) {
        if (obj == null || method == null)
            return null;
        try {
            var m = obj.getClass().getMethod(method);
            m.setAccessible(true);
            return m.invoke(obj);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String getStringField(Object obj, String field) {
        Object r = getObjectField(obj, field);
        return (r instanceof String) ? (String) r : null;
    }

    private static List<?> getListField(Object obj, String field) {
        Object r = getObjectField(obj, field);
        return (r instanceof List<?>) ? (List<?>) r : null;
    }

    private static Object getObjectField(Object obj, String field) {
        if (obj == null || field == null)
            return null;
        try {
            Class<?> c = obj.getClass();
            while (c != null) {
                try {
                    var f = c.getDeclaredField(field);
                    f.setAccessible(true);
                    return f.get(obj);
                } catch (NoSuchFieldException e) {
                    c = c.getSuperclass();
                }
            }
            return null;
        } catch (Exception ignored) {
            return null;
        }
    }

    public List<ASTNode> getGlobalDecls() {
        return declarations;
    }

    public List<ASTNode> getFunctions() {
        return functions;
    }

    public List<MainNode> getMainNodes() {
        return mainBlocks;
    }

    public MainNode getMainBlock() {
        return (mainBlocks != null && !mainBlocks.isEmpty()) ? mainBlocks.get(0) : null;
    }

}