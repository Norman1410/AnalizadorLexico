package semantics;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import ast.*;

public class MipsGenerator {

    private final Object semTab;
    // Secciones del .asm
    private final List<String> dataSection = new ArrayList<>();
    private final StringBuilder textSection = new StringBuilder();

    // Labels para control de flujo
    private int labelCounter = 0;
    private String exitMainLabel = null;

    private String newLabel(String prefix) {
        return prefix + "_" + (labelCounter++);
    }

    // Pool de strings para no duplicar labels
    private int strCounter = 0;
    private final Map<String, String> stringPool = new HashMap<>();

    // “Memoria” local de variables en main
    private final Map<String, Integer> localOffset = new HashMap<>();
    private final Map<String, String> localType = new HashMap<>();
    private int localBytes = 0;

    // Globales
    private final Map<String, String> globalLabel = new HashMap<>();
    private final Map<String, String> globalType = new HashMap<>();

    // arreglos: base offset y dims (ej: [10] o [2,3])
    private final Map<String, List<Integer>> localDims = new HashMap<>();
    private final Map<String, List<Integer>> globalDims = new HashMap<>();
    private String currentExitLabel = null;

    public MipsGenerator(Object semTab) {
        this.semTab = semTab;
    }

    public void generate(ProgramNode program, String rutaMips) throws IOException {
        if (program == null)
            throw new IllegalArgumentException("program null");
        if (rutaMips == null || rutaMips.isBlank())
            throw new IllegalArgumentException("rutaMips vacía");

        dataSection.clear();
        textSection.setLength(0);
        stringPool.clear();
        localOffset.clear();
        localType.clear();
        globalLabel.clear();
        globalType.clear();
        localBytes = 0;
        strCounter = 0;
        labelCounter = 0;
        exitMainLabel = null;
        currentExitLabel = null;

        // =====================
        // DATA base
        // =====================
        dataSection.add(".data");
        dataSection.add("nl: .asciiz \"\\n\"");
        emitGlobals(program);

        // =====================
        // TEXT base
        // =====================
        textSection.append(".text\n");
        textSection.append(".globl main\n\n");
        emitAllFunctions(program);
        textSection.append("main:\n");
        exitMainLabel = newLabel("exit_main");
        currentExitLabel = exitMainLabel;

        // mainBlock
        ASTNode mainAst = program.getMainBlock();
        BlockNode mainBlock = null;

        if (mainAst instanceof MainNode) {
            MainNode mn = (MainNode) mainAst;
            ASTNode blk = mn.getBlock();
            if (blk instanceof BlockNode) {
                BlockNode b = (BlockNode) blk;
                mainBlock = b;
            }
        } else if (mainAst instanceof BlockNode) {
            BlockNode b = (BlockNode) mainAst;
            mainBlock = b;
        }

        if (mainBlock == null) {
            textSection.append(exitMainLabel).append(":\n");
            textSection.append("    li $v0, 10\n");
            textSection.append("    syscall\n");
            writeAsm(rutaMips);
            return;
        }

        localDims.clear();
        // reservar espacio para locals
        preScanLocals(mainBlock);
        if (localBytes > 0) {
            textSection.append("    addi $sp, $sp, -").append(localBytes).append("\n");
            textSection.append("    move $s0, $sp\n"); // base locals
        }
        // emitir statements del main
        emitBlock(mainBlock);

        textSection.append(exitMainLabel).append(":\n");
        if (localBytes > 0) {
            textSection.append("    addi $sp, $sp, ").append(localBytes).append("\n");
        }

        // exit real
        textSection.append("    li $v0, 10\n");
        textSection.append("    syscall\n");

        writeAsm(rutaMips);
    }

    private void emitGlobals(ProgramNode program) {
        List<ASTNode> globals = program.getGlobalDecls();
        if (globals == null)
            return;

        for (ASTNode n : globals) {
            if (!(n instanceof DeclNode))
                continue;
            DeclNode dn = (DeclNode) n;

            String name = dn.getName();
            String type = dn.getTypeName();
            List<Integer> dims = dn.getDims();

            String label = "g_" + name;
            globalLabel.put(name, label);
            globalType.put(name, type);
            globalDims.put(name, dims);

            // Es arreglo?
            if (dims != null && !dims.isEmpty()) {
                int slots = 1;
                for (Integer d : dims)
                    slots *= (d == null ? 0 : d);
                dataSection.add(label + ": .space " + (slots * 4));
                continue;
            }

            if ("int".equalsIgnoreCase(type) || "boolean".equalsIgnoreCase(type) || "float".equalsIgnoreCase(type)) {
                int initVal = 0;
                ASTNode init = dn.getInitializer();
                if (init instanceof LiteralNode) {
                    LiteralNode lit = (LiteralNode) init;
                    if (lit.isInt()) {
                        initVal = lit.getIntValue();
                    }
                }
                dataSection.add(label + ": .word " + initVal);
                continue;
            }

            if ("string".equalsIgnoreCase(type)) {
                ASTNode init = dn.getInitializer();
                if (init instanceof LiteralNode) {
                    LiteralNode lit = (LiteralNode) init;
                    if (lit.isString()) {
                        String strLbl = internString(lit.getStringValue());
                        dataSection.add(label + ": .word " + strLbl);
                    } else {
                        dataSection.add(label + ": .word 0");
                    }
                } else {
                    dataSection.add(label + ": .word 0");
                }
            }
        }
    }

    private void preScanLocals(ASTNode node) {
        if (node == null)
            return;

        if (node instanceof DeclNode) {
            DeclNode dn = (DeclNode) node;
            if (!dn.isGlobal()) {
                String name = dn.getName();
                if (!localOffset.containsKey(name)) {
                    int slots = 1;
                    List<Integer> dims = dn.getDims();
                    if (dims != null && !dims.isEmpty()) {
                        for (Integer d : dims) {
                            if (d != null)
                                slots *= d;
                        }
                    }
                    localOffset.put(name, localBytes);
                    localType.put(name, dn.getTypeName());
                    localDims.put(name, dims);
                    localBytes += 4 * slots;
                }
            }
        } else if (node instanceof BlockNode) {
            BlockNode bn = (BlockNode) node;
            List<ASTNode> stmts = bn.getStatements();
            if (stmts != null) {
                for (ASTNode s : stmts) {
                    preScanLocals(s);
                }
            }
        } else if (node instanceof ForNode) {
            ForNode fn = (ForNode) node;
            preScanLocals(fn.getInit());
            preScanLocals(fn.getBody());
        } else if (node instanceof LoopNode) {
            LoopNode ln = (LoopNode) node;
            preScanLocals(ln.getBody());
        } else if (node instanceof DecideNode) {
            DecideNode dn = (DecideNode) node;
            if (dn.getCases() != null) {
                for (ASTNode c : dn.getCases()) {
                    if (c instanceof CaseNode) {
                        preScanLocals(((CaseNode) c).getBlock());
                    }
                }
            }
            preScanLocals(dn.getElseBlock());
        }
    }

    // -------------------------
    // Emit block
    // -------------------------
    private void emitBlock(BlockNode block) {
        List<ASTNode> stmts = block.getStatements();
        if (stmts == null)
            return;

        for (ASTNode s : stmts) {
            if (s == null)
                continue;

            if (s instanceof DeclNode) {
                handleDecl((DeclNode) s);
            } else if (s instanceof AssignNode) {
                handleAssign((AssignNode) s);
            } else if (s instanceof ShowNode) {
                handleShow((ShowNode) s);
            } else if (s instanceof CallNode) {
                handleCallStmt((CallNode) s);
            } else if (s instanceof DecideNode) {
                handleDecide((DecideNode) s);
            } else if (s instanceof LoopNode) {
                handleLoop((LoopNode) s);
            } else if (s instanceof ForNode) {
                handleFor((ForNode) s);
            } else if (s instanceof GetNode) {
                handleGet((GetNode) s);
            } else if (s instanceof ReturnNode) {
                handleReturn((ReturnNode) s);
            } else {
                textSection.append("    # ignorado: ").append(s.getClass().getSimpleName()).append("\n");
            }

        }
    }

    // -------------------------
    // DECL local
    // -------------------------
    private void handleDecl(DeclNode dn) {
        if (dn.isGlobal())
            return;

        ASTNode init = dn.getInitializer();
        if (init == null)
            return;

        if ("int".equals(dn.getTypeName())) {
            if (emitExprInt(init)) {
                storeVarFromT0(dn.getName());
            }
            return;
        }

        if ("string".equals(dn.getTypeName()) && init instanceof LiteralNode) {
            LiteralNode lit = (LiteralNode) init;
            if (lit.isString()) {
                String label = internString(lit.getStringValue());
                textSection.append("    la $t0, ").append(label).append("\n");
                storeVarFromT0(dn.getName());
            }
        }
    }

    // -------------------------
    // ASSIGN (solo variable por ahora)
    // -------------------------
    private void handleAssign(AssignNode an) {
        ASTNode target = an.getTarget();
        ASTNode expr = an.getExpression();

        // arreglo: a[i] = expr
        if (target != null && target.getClass().getSimpleName().equals("ArrayAccessNode")) {
            // 1) calcular dirección primero -> $t1
            if (!emitLValueAddress(target))
                return;
            textSection.append("    move $t6, $t1\n");

            // 2) calcular expr > $t0
            if (!emitExprInt(expr))
                return;

            // 3) store en la dirección
            textSection.append("    move $t1, $t6\n");
            textSection.append("    sw $t0, 0($t1)\n");
            return;
        }

        if (!(target instanceof VariableNode))
            return;
        VariableNode tv = (VariableNode) target;
        String dst = tv.getName();

        String dstType = localType.containsKey(dst)
                ? localType.get(dst)
                : globalType.getOrDefault(dst, "int");

        if ("int".equalsIgnoreCase(dstType)) {
            if (emitExprInt(expr)) {
                storeVarFromT0(dst);
            }
            return;
        }

        if ("string".equalsIgnoreCase(dstType)) {
            if (expr instanceof LiteralNode) {
                LiteralNode lit = (LiteralNode) expr;
                if (lit.isString()) {
                    String label = internString(lit.getStringValue());
                    textSection.append("    la $t0, ").append(label).append("\n");
                    storeVarFromT0(dst);
                    return;
                }
            }

            if (expr instanceof VariableNode) {
                VariableNode vn = (VariableNode) expr;
                loadVarToT0(vn.getName());
                storeVarFromT0(dst);
                return;
            }
            return;
        }

    }

    // -------------------------
    // SHOW
    // -------------------------
    private void handleShow(ShowNode show) {
        ASTNode expr = show.getExpression();

        // string literal
        if (expr instanceof LiteralNode) {
            LiteralNode lit = (LiteralNode) expr;
            if (lit.isString()) {
                String label = internString(lit.getStringValue());
                printStringLabel(label);
                printNewLine();
                return;
            }
            if (lit.isInt()) {
                printIntImm(lit.getIntValue());
                printNewLine();
                return;
            }
        }

        // mostrar arr[i] (int)
        if (expr != null && expr.getClass().getSimpleName().equals("ArrayAccessNode")) {
            if (emitLValueAddress(expr)) {
                textSection.append("    lw $t0, 0($t1)\n");
                textSection.append("    li $v0, 1\n");
                textSection.append("    move $a0, $t0\n");
                textSection.append("    syscall\n");
                printNewLine();
            }
            return;
        }

        // variable
        if (expr instanceof VariableNode) {
            VariableNode vn = (VariableNode) expr;
            String name = vn.getName();

            String type;
            if (localType.containsKey(name)) {
                type = localType.get(name);
            } else if (globalType.containsKey(name)) {
                type = globalType.get(name);
            } else {
                type = "int";
            }

            loadVarToT0(name);

            if ("string".equalsIgnoreCase(type)) {
                // imprime el string apuntado por $t0
                textSection.append("    li $v0, 4\n");
                textSection.append("    move $a0, $t0\n");
                textSection.append("    syscall\n");
                printNewLine();
                return;
            }

            // int (default)
            textSection.append("    li $v0, 1\n");
            textSection.append("    move $a0, $t0\n");
            textSection.append("    syscall\n");
            printNewLine();
            return;
        }

        // expresión int
        if (emitExprInt(expr)) {
            textSection.append("    li $v0, 1\n");
            textSection.append("    move $a0, $t0\n");
            textSection.append("    syscall\n");
            printNewLine();
            return;
        }

        textSection.append("    # show no soportado: ")
                .append(expr == null ? "null" : expr.getClass().getSimpleName())
                .append("\n");
    }

    private void handleCallStmt(CallNode cn) {
        emitCall(cn);
        // Si retorna algoo se ignora
    }

    private void handleDecide(DecideNode dn) {

        String endLbl = newLabel("decide_end");

        // evaluar cases en orden
        for (CaseNode c : dn.getCases()) {
            String nextLbl = newLabel("decide_next");
            ASTNode cond = c.getExpression();
            BlockNode blk = c.getBlock();
            emitCondBranch(cond, null, nextLbl);
            if (blk != null)
                emitBlock(blk);

            textSection.append("    j ").append(endLbl).append("\n");

            textSection.append(nextLbl).append(":\n");
        }

        // ningún case se cumplió
        if (dn.getElseBlock() instanceof BlockNode) {
            BlockNode eb = (BlockNode) dn.getElseBlock();
            emitBlock(eb);
        }

        textSection.append(endLbl).append(":\n");
    }

    private void handleLoop(LoopNode ln) {
        String loopStart = newLabel("loop_start");
        String loopEnd = newLabel("loop_end");

        textSection.append(loopStart).append(":\n");

        BlockNode body = ln.getBody();
        if (body != null) {
            emitBlock(body);
        }

        ASTNode cond = ln.getExitCondition();
        if (cond != null) {
            emitCondBranch(cond, loopEnd, loopStart);
        }

        textSection.append("    j ").append(loopStart).append("\n");
        textSection.append(loopEnd).append(":\n");
    }

    private void handleFor(ForNode fn) {
        String startLbl = newLabel("for_start");
        String endLbl = newLabel("for_end");

        ASTNode init = fn.getInit();
        if (init != null) {
            if (init instanceof DeclNode)
                handleDecl((DeclNode) init);
            else if (init instanceof AssignNode)
                handleAssign((AssignNode) init);
            else if (init instanceof BinaryNode)
                emitExprInt(init);
        }

        textSection.append(startLbl).append(":\n");

        ASTNode cond = fn.getCondition();
        if (cond != null) {
            emitCondBranch(cond, null, endLbl);
        }

        BlockNode body = fn.getBody();
        if (body != null)
            emitBlock(body);

        ASTNode step = fn.getStep();
        if (step != null) {
            if (step instanceof AssignNode)
                handleAssign((AssignNode) step);
            else if (step instanceof BinaryNode)
                emitExprInt(step);
        }

        textSection.append("    j ").append(startLbl).append("\n");
        textSection.append(endLbl).append(":\n");
    }

    private void handleGet(GetNode gn) {
        ASTNode target = gn.getTarget();
        if (target == null)
            return;

        // Calcular dirección destino -> $t1
        if (emitLValueAddress(target)) {
            // Guardar en pila para el syscall
            textSection.append("    addi $sp, $sp, -4\n");
            textSection.append("    sw $t1, 0($sp)\n");

            // Leer entero syscall 5
            textSection.append("    li $v0, 5\n");
            textSection.append("    syscall\n");
            textSection.append("    lw $t1, 0($sp)\n");
            textSection.append("    addi $sp, $sp, 4\n");
            textSection.append("    sw $v0, 0($t1)\n");
        }
    }

    private void handleReturn(ReturnNode rn) {
        ASTNode expr = rn.getExpression();

        if (expr != null) {
            if (emitExprInt(expr)) {
                textSection.append("    move $v0, $t0\n");
            } else {
                textSection.append("    li $v0, 0\n");
            }
        } else {
            textSection.append("    li $v0, 0\n");
        }
        textSection.append("    j ").append(currentExitLabel).append("\n");
    }

    // =========================
    // Expresiones int -> resultado en $t0
    // =========================
    private boolean emitExprInt(ASTNode expr) {
        if (expr == null)
            return false;

        // literal int
        if (expr instanceof LiteralNode) {
            LiteralNode lit = (LiteralNode) expr;
            if (!lit.isInt())
                return false;
            textSection.append("    li $t0, ").append(lit.getIntValue()).append("\n");
            return true;
        }
        // variable int
        if (expr instanceof VariableNode) {
            VariableNode vn = (VariableNode) expr;
            loadVarToT0(vn.getName());
            return true;
        }
        // unary
        if (expr instanceof UnaryNode) {
            UnaryNode un = (UnaryNode) expr;
            if (!emitExprInt(un.getExpression()))
                return false;

            String op = un.getOperator();
            if ("-".equals(op) || "neg".equalsIgnoreCase(op)) {
                textSection.append("    sub $t0, $zero, $t0\n");
                return true;
            }
            return false;
        }

        // binary
        if (expr instanceof BinaryNode) {
            BinaryNode bn = (BinaryNode) expr;
            String op = bn.getOperator();

            if (!emitExprInt(bn.getLeft()))
                return false;

            // push left
            textSection.append("    addi $sp, $sp, -4\n");
            textSection.append("    sw $t0, 0($sp)\n");

            if (!emitExprInt(bn.getRight()))
                return false;

            // pop left -> $t1
            textSection.append("    lw $t1, 0($sp)\n");
            textSection.append("    addi $sp, $sp, 4\n");

            switch (op) {
                case "+" -> textSection.append("    add $t0, $t1, $t0\n");
                case "-" -> textSection.append("    sub $t0, $t1, $t0\n");
                case "*" -> textSection.append("    mul $t0, $t1, $t0\n");
                case "/", "//" -> {
                    textSection.append("    div $t1, $t0\n");
                    textSection.append("    mflo $t0\n");
                }
                case "%" -> {
                    textSection.append("    div $t1, $t0\n");
                    textSection.append("    mfhi $t0\n");
                }
                default -> {
                    return false;
                }
            }
            return true;
        }
        if (expr instanceof CallNode) {
            CallNode cn = (CallNode) expr;
            emitCall(cn);
            textSection.append("    move $t0, $v0\n");
            return true;
        }

        // arr[i] o arr[i][j] como expresión int
        if (expr != null && isNodeNamed(expr, "ArrayAccessNode")) {
            if (emitLValueAddress(expr)) {
                textSection.append("    lw $t0, 0($t1)\n");
                return true;
            }
            return false;
        }

        return false;
    }

    // =========================
    // Condiciones boolean -> branches directos
    // =========================
    private void emitCondBranch(ASTNode expr, String trueLabel, String falseLabel) {
        if (falseLabel == null)
            throw new IllegalArgumentException("falseLabel null");

        if (expr == null) {
            if (falseLabel != null) {
                textSection.append("    j ").append(falseLabel).append("\n");
            }
            return;
        }

        // Soporte: && y || con short-circuit
        if (expr instanceof BinaryNode) {
            BinaryNode bn = (BinaryNode) expr;
            String op = bn.getOperator();

            if ("&&".equals(op)) {
                String rhsLbl = newLabel("and_rhs");
                String contLbl = (trueLabel != null) ? trueLabel : newLabel("and_cont");

                // si left es true evaluar right, si no false
                emitCondBranch(bn.getLeft(), rhsLbl, falseLabel);
                textSection.append(rhsLbl).append(":\n");
                emitCondBranch(bn.getRight(), contLbl, falseLabel);

                if (trueLabel == null) {
                    textSection.append(contLbl).append(":\n");
                }
                return;
            }

            if ("||".equals(op)) {
                String rhsLbl = newLabel("or_rhs");
                String contLbl = (trueLabel != null) ? trueLabel : newLabel("or_cont");

                // si left es true cont, si noevaluar right
                emitCondBranch(bn.getLeft(), contLbl, rhsLbl);
                textSection.append(rhsLbl).append(":\n");
                emitCondBranch(bn.getRight(), contLbl, falseLabel);

                if (trueLabel == null) {
                    textSection.append(contLbl).append(":\n");
                }
                return;
            }
            // Comparaciones
            if ("<".equals(op) || "<=".equals(op) || ">".equals(op) || ">=".equals(op) || "==".equals(op)
                    || "!=".equals(op)) {

                if (!emitExprInt(bn.getLeft())) {
                    if (falseLabel != null)
                        textSection.append("    j ").append(falseLabel).append("\n");
                    return;
                }

                // push left
                textSection.append("    addi $sp, $sp, -4\n");
                textSection.append("    sw $t0, 0($sp)\n");
                // right
                if (!emitExprInt(bn.getRight())) {
                    textSection.append("    addi $sp, $sp, 4\n");
                    if (falseLabel != null)
                        textSection.append("    j ").append(falseLabel).append("\n");
                    return;
                }

                // pop left -> $t1, right está en $t0
                textSection.append("    lw $t1, 0($sp)\n");
                textSection.append("    addi $sp, $sp, 4\n");

                // t2 = (t1 < t0) etc.
                switch (op) {
                    case "<" -> {
                        textSection.append("    slt $t2, $t1, $t0\n");
                        branchOnT2(trueLabel, falseLabel);
                    }
                    case ">" -> {
                        textSection.append("    slt $t2, $t0, $t1\n");
                        branchOnT2(trueLabel, falseLabel);
                    }
                    case "<=" -> {
                        // !(t0 < t1) <=> (t1 <= t0)
                        textSection.append("    slt $t2, $t0, $t1\n");
                        // t2 = 1 si t0 < t1
                        textSection.append("    xori $t2, $t2, 1\n");
                        branchOnT2(trueLabel, falseLabel);
                    }
                    case ">=" -> {
                        // !(t1 < t0) <=> (t1 >= t0)
                        textSection.append("    slt $t2, $t1, $t0\n");
                        textSection.append("    xori $t2, $t2, 1\n");
                        branchOnT2(trueLabel, falseLabel);
                    }
                    case "==" -> {
                        if (trueLabel != null) {
                            textSection.append("    beq $t1, $t0, ").append(trueLabel).append("\n");
                            if (falseLabel != null)
                                textSection.append("    j ").append(falseLabel).append("\n");
                        } else {
                            if (falseLabel != null) {
                                textSection.append("    bne $t1, $t0, ").append(falseLabel).append("\n");
                            }
                        }
                    }
                    case "!=" -> {
                        if (trueLabel != null) {
                            textSection.append("    bne $t1, $t0, ").append(trueLabel).append("\n");
                            if (falseLabel != null)
                                textSection.append("    j ").append(falseLabel).append("\n");
                        } else {
                            if (falseLabel != null) {
                                textSection.append("    beq $t1, $t0, ").append(falseLabel).append("\n");
                            }
                        }
                    }
                }
                return;
            }
        }
        if (falseLabel != null) {
            textSection.append("    j ").append(falseLabel).append("\n");
        }
    }

    private void branchOnT2(String trueLabel, String falseLabel) {
        if (falseLabel != null) {
            textSection.append("    beq $t2, $zero, ").append(falseLabel).append("\n");
        }

        if (trueLabel != null) {
            textSection.append("    j ").append(trueLabel).append("\n");
        }
    }

    // =========================
    // Helpers MIPS
    // =========================
    private void printNewLine() {
        textSection.append("    li $v0, 4\n");
        textSection.append("    la $a0, nl\n");
        textSection.append("    syscall\n");
    }

    private void printStringLabel(String label) {
        textSection.append("    li $v0, 4\n");
        textSection.append("    la $a0, ").append(label).append("\n");
        textSection.append("    syscall\n");
    }

    private void printIntImm(int value) {
        textSection.append("    li $v0, 1\n");
        textSection.append("    li $a0, ").append(value).append("\n");
        textSection.append("    syscall\n");
    }

    private void loadVarToT0(String name) {
        Integer off = localOffset.get(name);
        if (off != null) {
            textSection.append("    lw $t0, ").append(off).append("($s0)\n");
            return;
        }

        String gl = globalLabel.get(name);
        if (gl != null) {
            textSection.append("    lw $t0, ").append(gl).append("\n");
            return;
        }

        textSection.append("    li $t0, 0\n");
    }

    private void storeVarFromT0(String name) {
        Integer off = localOffset.get(name);
        if (off != null) {
            textSection.append("    sw $t0, ").append(off).append("($s0)\n");
            return;
        }

        String gl = globalLabel.get(name);
        if (gl != null) {
            textSection.append("    sw $t0, ").append(gl).append("\n");
        }
    }

    private boolean isNodeNamed(Object o, String simpleName) {
        return o != null && o.getClass().getSimpleName().equals(simpleName);
    }

    private Object callNoArg(Object o, String method) {
        if (o == null)
            return null;
        try {
            var m = o.getClass().getMethod(method);
            m.setAccessible(true);
            return m.invoke(o);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private List<ASTNode> callAstList(Object o, String method) {
        Object r = callNoArg(o, method);
        return (r instanceof List<?>) ? (List<ASTNode>) r : null;
    }

    private boolean emitLValueAddress(ASTNode target) {
        // Variable simple
        if (target instanceof VariableNode) {
            VariableNode vn = (VariableNode) target;
            String name = vn.getName();

            Integer off = localOffset.get(name);
            if (off != null) {
                textSection.append("    addi $t1, $s0, ").append(off).append("\n");
                return true;
            }

            String gl = globalLabel.get(name);
            if (gl != null) {
                textSection.append("    la $t1, ").append(gl).append("\n");
                return true;
            }

            textSection.append("    li $t1, 0\n");
            return false;
        }

        // ArrayAccessNode
        if (isNodeNamed(target, "ArrayAccessNode")) {
            ASTNode base = (ASTNode) callNoArg(target, "getArray"); // típico
            List<ASTNode> idx = callAstList(target, "getIndices"); // típico

            if (base == null)
                base = (ASTNode) callNoArg(target, "getBase");
            if (idx == null)
                idx = callAstList(target, "getIndexList");

            if (base == null || idx == null || idx.isEmpty()) {
                textSection.append("    li $t1, 0\n");
                return false;
            }

            String arrName = null;
            if (base instanceof VariableNode) {
                VariableNode bvn = (VariableNode) base;
                arrName = bvn.getName();
            }
            if (arrName == null) {
                textSection.append("    li $t1, 0\n");
                return false;
            }

            // cargar base address del arreglo en $t1
            Integer off = localOffset.get(arrName);
            if (off != null) {
                textSection.append("    addi $t1, $s0, ").append(off).append("\n");
            } else {
                String gl = globalLabel.get(arrName);
                if (gl == null) {
                    textSection.append("    li $t1, 0\n");
                    return false;
                }
                textSection.append("    la $t1, ").append(gl).append("\n");
            }

            // dims para 2D
            List<Integer> dims = (off != null) ? localDims.get(arrName) : globalDims.get(arrName);
            int cols = -1;
            if (dims != null && dims.size() >= 2 && dims.get(1) != null) {
                cols = dims.get(1);
            }

            // si es acceso 2D y no hay cols válido, abortar
            if (idx.size() >= 2 && cols <= 0) {
                textSection.append("    li $t1, 0\n");
                return false;
            }

            // idx0
            if (!emitExprInt(idx.get(0)))
                return false; // deja resultado en $t0
            textSection.append("    move $t2, $t0\n");

            if (idx.size() >= 2) {
                if (!emitExprInt(idx.get(1)))
                    return false;
                textSection.append("    move $t3, $t0\n");

                // t2 = t2*cols + t3
                textSection.append("    li $t4, ").append(cols).append("\n");
                textSection.append("    mul $t2, $t2, $t4\n");
                textSection.append("    addu $t2, $t2, $t3\n");
            }

            // byte offset = linear * 4
            textSection.append("    sll $t2, $t2, 2\n");
            textSection.append("    addu $t1, $t1, $t2\n");
            return true;
        }

        textSection.append("    li $t1, 0\n");
        return false;
    }

    // =========================
    // Strings
    // =========================
    private String internString(String raw) {
        if (raw == null)
            raw = "";
        if (stringPool.containsKey(raw))
            return stringPool.get(raw);

        String label = "str_" + (strCounter++);
        stringPool.put(raw, label);

        dataSection.add(label + ": .asciiz \"" + escapeForAsciiz(raw) + "\"");
        return label;
    }

    private String escapeForAsciiz(String s) {
        return s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // =========================
    // Literal parsing
    // =========================
    private int parseIntLiteral(LiteralNode lit) {
        Object v = getField(lit, "value");
        if (v instanceof Integer) {
            return (Integer) v;
        }
        if (v instanceof String) {
            String s = (String) v;
            try {
                return Integer.parseInt(s.replace("\"", "").trim());
            } catch (Exception ignored) {
            }
        }
        return 0;
    }

    private String parseStringLiteral(LiteralNode lit) {
        Object v = getField(lit, "value");
        if (v instanceof String) {
            String s = (String) v;
            if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
                return s.substring(1, s.length() - 1);
            }
            return s;
        }
        return "";
    }

    private Object getField(Object obj, String fieldName) {
        if (obj == null)
            return null;
        try {
            Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(obj);
        } catch (Exception e) {
            return null;
        }
    }

    private void emitAllFunctions(ProgramNode program) {
        List<ASTNode> fns = program.getFunctions();
        if (fns == null)
            return;

        for (ASTNode n : fns) {
            if (n instanceof FunctionNode) {
                emitFunction((FunctionNode) n);
            }
        }
    }

    private void emitFunction(FunctionNode fn) {
        String fname = fn.getName();
        String exitLbl = "exit_func_" + fname;

        textSection.append("\n").append(fname).append(":\n");

        // Guardaa $ra y $s0
        textSection.append("    addi $sp, $sp, -8\n");
        textSection.append("    sw $ra, 4($sp)\n");
        textSection.append("    sw $s0, 0($sp)\n");

        // Reset estado de frame de función
        localOffset.clear();
        localType.clear();
        localDims.clear();
        localBytes = 0;
        BlockNode body = fn.getBlock();

        // Reservar espacio para params como locals primero
        List<ParamNode> ps = fn.getParams();
        int paramCount = (ps == null) ? 0 : ps.size();
        for (int i = 0; i < paramCount; i++) {
            ParamNode p = ps.get(i);
            String pname = p.getName();
            String ptype = p.getType();

            localOffset.put(pname, localBytes);
            localType.put(pname, ptype);
            localDims.put(pname, List.of());
            localBytes += 4;
        }
        if (body != null) {
            preScanLocals(body);
        }
        int frameBytes = localBytes;
        if (frameBytes > 0) {
            textSection.append("    addi $sp, $sp, -").append(frameBytes).append("\n");
        }
        textSection.append("    move $s0, $sp\n");
        bindParamsAsLocals(fn, frameBytes);

        String prevExit = currentExitLabel;
        currentExitLabel = exitLbl;

        if (body != null)
            emitBlock(body);

        textSection.append("    li $v0, 0\n");
        textSection.append("    j ").append(exitLbl).append("\n");

        // Epílogo
        textSection.append(exitLbl).append(":\n");

        if (frameBytes > 0) {
            textSection.append("    addi $sp, $sp, ").append(frameBytes).append("\n");
        }

        textSection.append("    lw $s0, 0($sp)\n");
        textSection.append("    lw $ra, 4($sp)\n");
        textSection.append("    addi $sp, $sp, 8\n");
        textSection.append("    jr $ra\n");

        currentExitLabel = prevExit;
    }

    private void bindParamsAsLocals(FunctionNode fn, int frameBytes) {
        List<ParamNode> ps = fn.getParams();
        if (ps == null || ps.isEmpty())
            return;

        int argsBase = frameBytes + 8;

        for (int i = 0; i < ps.size(); i++) {
            ParamNode p = ps.get(i);
            String name = p.getName();

            Integer dstOff = localOffset.get(name);
            if (dstOff == null)
                continue;

            // arg i está en: ($sp + argsBase + i*4)
            textSection.append("    lw $t0, ").append(argsBase + i * 4).append("($sp)\n");
            textSection.append("    sw $t0, ").append(dstOff).append("($s0)\n");
        }
    }

    private void emitCall(CallNode cn) {
        String fname = cn.getName();
        List<ASTNode> args = cn.getArguments();
        int n = (args == null) ? 0 : args.size();

        for (int i = 0; i < n; i++) {
            ASTNode a = args.get(i);
            if (!emitExprInt(a)) {
                textSection.append("    li $t0, 0\n");
            }
            textSection.append("    addi $sp, $sp, -4\n");
            textSection.append("    sw $t0, 0($sp)\n");
        }

        textSection.append("    jal ").append(fname).append("\n");

        if (n > 0) {
            textSection.append("    addi $sp, $sp, ").append(n * 4).append("\n");
        }
    }

    private void writeAsm(String rutaMips) throws IOException {
        StringBuilder asm = new StringBuilder();
        for (String line : dataSection)
            asm.append(line).append("\n");
        asm.append("\n");
        asm.append(textSection);

        Files.writeString(Path.of(rutaMips), asm.toString(), StandardCharsets.UTF_8);
    }

}
