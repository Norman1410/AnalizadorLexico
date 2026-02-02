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

    private static class RegisterResult {
        String reg;
        String type;

        RegisterResult(String r, String t) {
            this.reg = r;
            this.type = t;
        }
    }

    private RegisterResult emitExpr(ASTNode expr) {
        if (expr == null)
            return null;

        // Literal
        if (expr instanceof LiteralNode) {
            return emitLiteral((LiteralNode) expr);
        }

        // Variable
        if (expr instanceof VariableNode) {
            return emitVariable((VariableNode) expr);
        }

        // Binary Operation
        if (expr instanceof BinaryNode) {
            return emitBinary((BinaryNode) expr);
        }

        // Unary Operation
        if (expr instanceof UnaryNode) {
            return emitUnary((UnaryNode) expr);
        }

        // Function Call
        if (expr instanceof CallNode) {
            return emitFunctionCall((CallNode) expr);
        }

        // Array Access
        if (isNodeNamed(expr, "ArrayAccessNode")) {
            return emitArrayAccess(expr);
        }

        return null;
    }

    private RegisterResult emitLiteral(LiteralNode lit) {
        if (lit.isFloat()) {
            String lbl = internFloat(lit.getFloatValue());
            textSection.append("    l.s $f0, ").append(lbl).append("\n");
            return new RegisterResult("$f0", "float");
        }
        if (lit.isInt()) {
            textSection.append("    li $t0, ").append(lit.getIntValue()).append("\n");
            return new RegisterResult("$t0", "int");
        }
        if (lit.isChar()) {
            textSection.append("    li $t0, ").append(lit.getCharValue()).append("\n");
            return new RegisterResult("$t0", "char");
        }
        if (lit.isString()) {
            String lbl = internString(lit.getStringValue());
            textSection.append("    la $t0, ").append(lbl).append("\n");
            return new RegisterResult("$t0", "string");
        }
        String type = lit.getType(null);
        if ("boolean".equalsIgnoreCase(type)) {
            boolean val = "true".equalsIgnoreCase(lit.getStringValue());
            textSection.append("    li $t0, ").append(val ? 1 : 0).append("\n");
            return new RegisterResult("$t0", "boolean");
        }
        return null;
    }

    private RegisterResult emitVariable(VariableNode vn) {
        String name = vn.getName();
        String type;
        if (localType.containsKey(name)) {
            type = localType.get(name);
        } else {
            type = globalType.getOrDefault(name, "int");
        }

        if ("float".equalsIgnoreCase(type)) {
            loadVarToFloat(name);
            return new RegisterResult("$f0", "float");
        } else {
            loadVarToT0(name);
            return new RegisterResult("$t0", type);
        }
    }

    private void pushRegister(String reg, String type) {
        textSection.append("    addi $sp, $sp, -4\n");
        if ("float".equalsIgnoreCase(type)) {
            textSection.append("    swc1 ").append(reg).append(", 0($sp)\n");
        } else {
            textSection.append("    sw ").append(reg).append(", 0($sp)\n");
        }
    }

    private String popRegister(String targetReg, String type) {
        if ("float".equalsIgnoreCase(type)) {
            textSection.append("    lwc1 ").append(targetReg).append(", 0($sp)\n");
        } else {
            textSection.append("    lw ").append(targetReg).append(", 0($sp)\n");
        }
        textSection.append("    addi $sp, $sp, 4\n");
        return targetReg;
    }

    private RegisterResult emitBinary(BinaryNode bn) {
        String op = bn.getOperator();

        RegisterResult lhs = emitExpr(bn.getLeft());
        if (lhs == null)
            return null;

        if (op.equals("&&") || op.equals("||") || op.equals("@") || op.equals("~")
                || op.equalsIgnoreCase("AND") || op.equalsIgnoreCase("OR")) {
            return emitLogicalShortCircuit(lhs, bn.getRight(), op);
        }

        pushRegister(lhs.reg, lhs.type);

        RegisterResult rhs = emitExpr(bn.getRight());
        if (rhs == null) {
            return null;
        }
        boolean useFloat = "float".equalsIgnoreCase(lhs.type) || "float".equalsIgnoreCase(rhs.type);

        if (useFloat) {

            if ("float".equalsIgnoreCase(rhs.type)) {
                textSection.append("    mov.s $f1, ").append(rhs.reg).append("\n");
            } else {

                textSection.append("    mtc1 ").append(rhs.reg).append(", $f1\n");
                textSection.append("    cvt.s.w $f1, $f1\n");
            }

            if ("float".equalsIgnoreCase(lhs.type)) {
                popRegister("$f0", "float");
            } else {

                popRegister("$t0", "int");
                textSection.append("    mtc1 $t0, $f0\n");
                textSection.append("    cvt.s.w $f0, $f0\n");
            }

            switch (op) {
                case "+":
                    textSection.append("    add.s $f0, $f0, $f1\n");
                    break;
                case "-":
                    textSection.append("    sub.s $f0, $f0, $f1\n");
                    break;
                case "*":
                    textSection.append("    mul.s $f0, $f0, $f1\n");
                    break;
                case "/":
                    textSection.append("    div.s $f0, $f0, $f1\n");
                    break;
                case "<":
                    textSection.append("    c.lt.s $f0, $f1\n");
                    String lElse = newLabel("flt_rel_e");
                    String lEnd = newLabel("flt_rel_f");
                    textSection.append("    bc1t ").append(lElse).append("\n");
                    textSection.append("    li $t0, 0\n");
                    textSection.append("    j ").append(lEnd).append("\n");
                    textSection.append(lElse).append(":\n");
                    textSection.append("    li $t0, 1\n");
                    textSection.append(lEnd).append(":\n");
                    return new RegisterResult("$t0", "boolean");
                case ">":
                    textSection.append("    c.lt.s $f1, $f0\n");
                    String lElse2 = newLabel("flt_rel_e");
                    String lEnd2 = newLabel("flt_rel_f");
                    textSection.append("    bc1t ").append(lElse2).append("\n");
                    textSection.append("    li $t0, 0\n");
                    textSection.append("    j ").append(lEnd2).append("\n");
                    textSection.append(lElse2).append(":\n");
                    textSection.append("    li $t0, 1\n");
                    textSection.append(lEnd2).append(":\n");
                    return new RegisterResult("$t0", "boolean");
                case "<=":
                    textSection.append("    c.le.s $f0, $f1\n");
                    String lElseLE = newLabel("flt_rel_e");
                    String lEndLE = newLabel("flt_rel_f");
                    textSection.append("    bc1t ").append(lElseLE).append("\n");
                    textSection.append("    li $t0, 0\n");
                    textSection.append("    j ").append(lEndLE).append("\n");
                    textSection.append(lElseLE).append(":\n");
                    textSection.append("    li $t0, 1\n");
                    textSection.append(lEndLE).append(":\n");
                    return new RegisterResult("$t0", "boolean");
                case ">=":
                    textSection.append("    c.le.s $f1, $f0\n");
                    String lElseGE = newLabel("flt_rel_e");
                    String lEndGE = newLabel("flt_rel_f");
                    textSection.append("    bc1t ").append(lElseGE).append("\n");
                    textSection.append("    li $t0, 0\n");
                    textSection.append("    j ").append(lEndGE).append("\n");
                    textSection.append(lElseGE).append(":\n");
                    textSection.append("    li $t0, 1\n");
                    textSection.append(lEndGE).append(":\n");
                    return new RegisterResult("$t0", "boolean");
                case "==":
                    textSection.append("    c.eq.s $f0, $f1\n");
                    String lElseEQ = newLabel("flt_rel_e");
                    String lEndEQ = newLabel("flt_rel_f");
                    textSection.append("    bc1t ").append(lElseEQ).append("\n");
                    textSection.append("    li $t0, 0\n");
                    textSection.append("    j ").append(lEndEQ).append("\n");
                    textSection.append(lElseEQ).append(":\n");
                    textSection.append("    li $t0, 1\n");
                    textSection.append(lEndEQ).append(":\n");
                    return new RegisterResult("$t0", "boolean");
                case "!=":
                    textSection.append("    c.eq.s $f0, $f1\n");
                    String lElseNE = newLabel("flt_rel_e");
                    String lEndNE = newLabel("flt_rel_f");
                    textSection.append("    bc1f ").append(lElseNE).append("\n");
                    textSection.append("    li $t0, 0\n");
                    textSection.append("    j ").append(lEndNE).append("\n");
                    textSection.append(lElseNE).append(":\n");
                    textSection.append("    li $t0, 1\n");
                    textSection.append(lEndNE).append(":\n");
                    return new RegisterResult("$t0", "boolean");
                default:
                    return null;
            }
            return new RegisterResult("$f0", "float");
        }

        else {

            textSection.append("    move $t1, ").append(rhs.reg).append("\n");
            popRegister("$t0", "int");

            switch (op) {
                case "+":
                    textSection.append("    add $t0, $t0, $t1\n");
                    break;
                case "-":
                    textSection.append("    sub $t0, $t0, $t1\n");
                    break;
                case "*":
                    textSection.append("    mul $t0, $t0, $t1\n");
                    break;
                case "/":
                    textSection.append("    div $t0, $t1\n");
                    textSection.append("    mflo $t0\n");
                    break;
                case "%":
                    textSection.append("    div $t0, $t1\n");
                    textSection.append("    mfhi $t0\n");
                    break;
                case "<":
                    textSection.append("    slt $t0, $t0, $t1\n");
                    break;
                case "<=":
                    textSection.append("    slt $t0, $t1, $t0\n");
                    textSection.append("    xori $t0, $t0, 1\n");
                    break;
                case ">":
                    textSection.append("    slt $t0, $t1, $t0\n");
                    break;
                case ">=":
                    textSection.append("    slt $t0, $t0, $t1\n");
                    textSection.append("    xori $t0, $t0, 1\n");
                    break;
                case "==":
                    textSection.append("    xor $t0, $t0, $t1\n");
                    textSection.append("    sltiu $t0, $t0, 1\n");
                    break;
                case "!=":
                    textSection.append("    xor $t0, $t0, $t1\n");
                    textSection.append("    sltu $t0, $zero, $t0\n");
                    break;
                case "@":
                case "AND":
                case "~":
                case "OR":
                    return null;
                case "^":
                    String lblLoop = newLabel("pow_loop");
                    String lblEnd = newLabel("pow_end");

                    textSection.append("    li $t2, 1\n");
                    textSection.append(lblLoop).append(":\n");
                    textSection.append("    beq $t1, $zero, ").append(lblEnd).append("\n");
                    textSection.append("    mul $t2, $t2, $t0\n");
                    textSection.append("    addi $t1, $t1, -1\n");
                    textSection.append("    j ").append(lblLoop).append("\n");
                    textSection.append(lblEnd).append(":\n");
                    textSection.append("    move $t0, $t2\n");
                    break;
                default:
                    return null;
            }
            return new RegisterResult("$t0", "int");
        }
    }

    private RegisterResult emitLogicalShortCircuit(RegisterResult lhs, ASTNode rightExpr, String op) {
        String endLbl = newLabel("logic_end");
        if (!"$t0".equals(lhs.reg)) {
            if ("float".equalsIgnoreCase(lhs.type)) {
                textSection.append("    cvt.w.s $f0, ").append(lhs.reg).append("\n");
                textSection.append("    mfc1 $t0, $f0\n");
            } else {
                textSection.append("    move $t0, ").append(lhs.reg).append("\n");
            }
        }

        if (op.equals("@") || op.equalsIgnoreCase("AND") || op.equals("&&")) {
            textSection.append("    beq $t0, $zero, ").append(endLbl).append("\n");
        } else {
            textSection.append("    bne $t0, $zero, ").append(endLbl).append("\n");
        }
        RegisterResult rhs = emitExpr(rightExpr);
        if (rhs == null)
            return null;
        if (!"$t0".equals(rhs.reg)) {
            if ("float".equalsIgnoreCase(rhs.type)) {
                textSection.append("    cvt.w.s $f0, ").append(rhs.reg).append("\n");
                textSection.append("    mfc1 $t0, $f0\n");
            } else {
                textSection.append("    move $t0, ").append(rhs.reg).append("\n");
            }
        }

        textSection.append(endLbl).append(":\n");
        return new RegisterResult("$t0", "boolean");
    }

    private RegisterResult emitUnary(UnaryNode un) {
        String op = un.getOperator();
        RegisterResult res = emitExpr(un.getExpression());
        if (res == null)
            return null;

        if ("float".equalsIgnoreCase(res.type)) {
            if ("-".equals(op)) {
                textSection.append("    neg.s ").append(res.reg).append(", ").append(res.reg).append("\n");
                return res;
            }
            if (op.contains("++") || op.contains("--")) {
                ASTNode operand = un.getExpression();
                if (!emitLValueAddress(operand))
                    return null;
                textSection.append("    l.s $f0, 0($t1)\n");

                String lblOne = internFloat(1.0f);
                textSection.append("    l.s $f1, ").append(lblOne).append("\n");
                if (!un.isPrefix()) {

                    if (op.contains("++"))
                        textSection.append("    add.s $f0, $f0, $f1\n");
                    else
                        textSection.append("    sub.s $f0, $f0, $f1\n");

                    textSection.append("    s.s $f0, 0($t1)\n");
                    if (op.contains("++"))
                        textSection.append("    sub.s $f0, $f0, $f1\n");
                    else
                        textSection.append("    add.s $f0, $f0, $f1\n");

                } else {
                    if (op.contains("++"))
                        textSection.append("    add.s $f0, $f0, $f1\n");
                    else
                        textSection.append("    sub.s $f0, $f0, $f1\n");

                    textSection.append("    s.s $f0, 0($t1)\n");
                }
                return new RegisterResult("$f0", "float");
            }
        } else {
            if ("-".equals(op)) {
                textSection.append("    neg ").append(res.reg).append(", ").append(res.reg).append("\n");
                return res;
            }
            if ("!".equals(op) || "NOT".equalsIgnoreCase(op)) {
                textSection.append("    xori ").append(res.reg).append(", ").append(res.reg).append(", 1\n");
                return new RegisterResult(res.reg, "boolean");
            }
            if (op.contains("++") || op.contains("--")) {
                ASTNode operand = un.getExpression();
                if (!emitLValueAddress(operand))
                    return null;
                String type = getLValueType(operand);
                boolean isChar = "char".equalsIgnoreCase(type);

                if (isChar)
                    textSection.append("    lb $t0, 0($t1)\n");
                else
                    textSection.append("    lw $t0, 0($t1)\n");

                if (!un.isPrefix()) {
                    if (op.contains("++"))
                        textSection.append("    addi $t2, $t0, 1\n");
                    else
                        textSection.append("    addi $t2, $t0, -1\n");
                    if (isChar)
                        textSection.append("    sb $t2, 0($t1)\n");
                    else
                        textSection.append("    sw $t2, 0($t1)\n");
                    return new RegisterResult("$t0", "int");
                } else {
                    if (op.contains("++"))
                        textSection.append("    addi $t0, $t0, 1\n");
                    else
                        textSection.append("    addi $t0, $t0, -1\n");
                    if (isChar)
                        textSection.append("    sb $t0, 0($t1)\n");
                    else
                        textSection.append("    sw $t0, 0($t1)\n");

                    return new RegisterResult("$t0", "int");
                }
            }
        }
        return res;
    }

    private RegisterResult emitFunctionCall(CallNode cn) {
        String fname = cn.getName();
        List<ASTNode> args = cn.getArguments();
        int n = (args == null) ? 0 : args.size();

        // Push args in reverse order
        for (int i = n - 1; i >= 0; i--) {
            ASTNode a = args.get(i);
            RegisterResult res = emitExpr(a);
            if (res != null) {
                pushRegister(res.reg, res.type);
            } else {
                // Push dummy 0 if error?
                textSection.append("    addi $sp, $sp, -4\n");
                textSection.append("    sw $zero, 0($sp)\n");
            }
        }

        textSection.append("    jal ").append(fname).append("\n");
        if (n > 0) {
            textSection.append("    addi $sp, $sp, ").append(n * 4).append("\n");
        }

        String retType = cn.getType((semantics.SymbolTable) semTab);
        if (retType == null)
            retType = "int";

        if ("float".equalsIgnoreCase(retType)) {

            return new RegisterResult("$f0", "float");
        } else {

            textSection.append("    move $t0, $v0\n");
            return new RegisterResult("$t0", retType);
        }
    }

    private RegisterResult emitArrayAccess(ASTNode expr) {

        if (emitLValueAddress(expr)) {
            String type = getLValueType(expr);
            if ("float".equalsIgnoreCase(type)) {
                textSection.append("    l.s $f0, 0($t1)\n");
                return new RegisterResult("$f0", "float");
            } else if ("char".equalsIgnoreCase(type)) {
                textSection.append("    lb $t0, 0($t1)\n");
                return new RegisterResult("$t0", "char");
            } else {
                textSection.append("    lw $t0, 0($t1)\n");
                return new RegisterResult("$t0", type);
            }
        }
        return null;
    }

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
    private String currentReturnType = null;

    // Control de break
    private final Stack<String> breakStack = new Stack<>();

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
        breakStack.clear();

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
        localOffset.clear();
        localType.clear();
        localBytes = 0;
        // reservar espacio para locals
        preScanLocals(mainBlock);
        if (localBytes > 0) {
            textSection.append("    addi $sp, $sp, -4\n");
            textSection.append("    sw $fp, 0($sp)\n");
            textSection.append("    move $fp, $sp\n");
            textSection.append("    addi $sp, $sp, -4\n");
            textSection.append("    sw $ra, 0($sp)\n");
            textSection.append("    addi $sp, $sp, -").append((localBytes + 7) & ~7).append("\n");
        } else {
            textSection.append("    addi $sp, $sp, -4\n");
            textSection.append("    sw $fp, 0($sp)\n");
            textSection.append("    move $fp, $sp\n");
            textSection.append("    addi $sp, $sp, -4\n");
            textSection.append("    sw $ra, 0($sp)\n");
        }
        // emitir statements del main
        emitBlock(mainBlock);

        textSection.append(exitMainLabel).append(":\n");
        textSection.append("    lw $ra, -4($fp)\n");
        textSection.append("    move $sp, $fp\n");
        textSection.append("    lw $fp, 0($sp)\n");
        textSection.append("    addi $sp, $sp, 4\n");

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
            dataSection.add(".align 2");

            String name = dn.getName();
            String type = dn.getTypeName();
            List<Integer> dims = dn.getDims();

            String label = "g_" + name;
            globalLabel.put(name, label);
            globalType.put(name, type);
            if (dims != null)
                globalDims.put(name, dims);
            if (dims != null && !dims.isEmpty()) {
                int slots = 1;
                for (Integer d : dims)
                    slots *= (d == null ? 0 : d);

                ASTNode init = dn.getInitializer();
                if (init instanceof ArrayLiteralNode) {
                    StringBuilder line = new StringBuilder(label + ": ");
                    String directive = "char".equalsIgnoreCase(type) ? ".byte " : ".word ";
                    line.append(directive);

                    List<ASTNode> flattened = flattenArrayLiteral((ArrayLiteralNode) init);
                    for (int i = 0; i < flattened.size(); i++) {
                        ASTNode elem = flattened.get(i);
                        if ("char".equalsIgnoreCase(type)) {
                            line.append(elem instanceof LiteralNode ? ((LiteralNode) elem).getCharValue() : 0);
                        } else if ("float".equalsIgnoreCase(type)) {
                            line.append(elem instanceof LiteralNode ? ((LiteralNode) elem).getFloatValue() : 0);
                        } else {
                            line.append(elem instanceof LiteralNode ? ((LiteralNode) elem).getIntValue() : 0);
                        }
                        if (i < flattened.size() - 1)
                            line.append(", ");
                    }
                    // Si el literal es más pequeño que el espacio reservado, rellenar?
                    // Por ahora asumimos que el parser valida dimensiones.
                    dataSection.add(line.toString());
                } else {
                    int stride = "char".equalsIgnoreCase(type) ? 1 : 4;
                    String directive = stride == 1 ? ".byte 0:" : ".word 0:";
                    dataSection.add(label + ": " + directive + slots);
                }
                continue;
            }

            if ("int".equalsIgnoreCase(type) || "boolean".equalsIgnoreCase(type)) {
                int initVal = 0;
                ASTNode init = dn.getInitializer();
                if (init instanceof LiteralNode) {
                    LiteralNode lit = (LiteralNode) init;
                    if (lit.isInt()) {
                        initVal = lit.getIntValue();
                    } else if (lit.isBoolean()) {
                        initVal = lit.getBooleanValue() ? 1 : 0;
                    }
                }
                dataSection.add(label + ": .word " + initVal);
                continue;
            }

            if ("float".equalsIgnoreCase(type)) {
                float initVal = 0.0f;
                ASTNode init = dn.getInitializer();
                if (init instanceof LiteralNode) {
                    LiteralNode lit = (LiteralNode) init;
                    if (lit.isFloat()) {
                        initVal = lit.getFloatValue();
                    } else if (lit.isInt()) {
                        initVal = (float) lit.getIntValue();
                    }
                }
                dataSection.add(label + ": .float " + initVal);
                continue;
            }

            if ("char".equalsIgnoreCase(type)) {
                int initVal = 0;
                ASTNode init = dn.getInitializer();
                if (init instanceof LiteralNode) {
                    LiteralNode lit = (LiteralNode) init;
                    if (lit.isChar()) {
                        initVal = lit.getCharValue();
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
                    int currentOff = -8 - localBytes;
                    localOffset.put(name, currentOff);
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
            } else if (s instanceof BreakNode) {
                handleBreak((BreakNode) s);
            } else if (s instanceof ReturnNode) {
                handleReturn((ReturnNode) s);
            } else if (s instanceof UnaryNode) {
                String type = getNodeType(s);
                if ("float".equalsIgnoreCase(type)) {
                    emitExprFloat(s);
                } else {
                    emitExprInt(s);
                }
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
        localType.put(dn.getName(), dn.getTypeName());

        ASTNode init = dn.getInitializer();
        if (init == null)
            return;

        // Array initialization
        if (init instanceof ArrayLiteralNode) {
            ArrayLiteralNode aln = (ArrayLiteralNode) init;
            handleArrayInitialization(dn.getName(), aln);
            return;
        }

        // Boolean type - use emitExprBool
        if ("boolean".equals(dn.getTypeName())) {
            if (emitExprBool(init)) {
                storeVarFromT0(dn.getName());
            }
            return;
        }

        if ("int".equals(dn.getTypeName()) || "char".equals(dn.getTypeName())) {
            if (emitExprInt(init)) {
                storeVarFromT0(dn.getName());
            }
            return;
        }

        if ("float".equals(dn.getTypeName())) {
            if (emitExprFloat(init)) {
                storeVarFromFloat(dn.getName());
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
        if (target != null && isNodeNamed(target, "ArrayAccessNode")) {
            // 1) Cargar el valor del índice y calcular la dirección base
            if (!emitLValueAddress(target))
                return;

            // Guardar la dirección calculada ($t1) en la pila por si emitExpr la clava
            textSection.append("    addi $sp, $sp, -4\n");
            textSection.append("    sw $t1, 0($sp)\n");

            String type = getLValueType(target);
            if ("float".equalsIgnoreCase(type)) {
                if (!emitExprFloat(expr)) {
                    textSection.append("    addi $sp, $sp, 4\n");
                    return;
                }
                textSection.append("    lw $t1, 0($sp)\n");
                textSection.append("    addi $sp, $sp, 4\n");
                textSection.append("    s.s $f0, 0($t1)\n");
            } else {
                if (!emitExprInt(expr)) {
                    textSection.append("    addi $sp, $sp, 4\n");
                    return;
                }
                textSection.append("    lw $t1, 0($sp)\n");
                textSection.append("    addi $sp, $sp, 4\n");
                if ("char".equalsIgnoreCase(type)) {
                    textSection.append("    sb $t0, 0($t1)\n");
                } else {
                    textSection.append("    sw $t0, 0($t1)\n");
                }
            }
            return;
        }

        if (!(target instanceof VariableNode))
            return;
        VariableNode tv = (VariableNode) target;
        String dst = tv.getName();

        String dstType = localType.containsKey(dst)
                ? localType.get(dst)
                : globalType.getOrDefault(dst, "int");

        if ("int".equalsIgnoreCase(dstType) || "char".equalsIgnoreCase(dstType)) {
            if (emitExprInt(expr)) {
                storeVarFromT0(dst);
            }
            return;
        }

        if ("boolean".equalsIgnoreCase(dstType)) {
            if (emitExprBool(expr)) {
                storeVarFromT0(dst);
            }
            return;
        }

        if ("float".equalsIgnoreCase(dstType)) {
            if (emitExprFloat(expr)) {
                storeVarFromFloat(dst);
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

        RegisterResult res = emitExpr(expr);
        if (res == null)
            return;

        if ("float".equalsIgnoreCase(res.type)) {
            if (!"$f12".equals(res.reg)) {
                textSection.append("    mov.s $f12, ").append(res.reg).append("\n");
            }
            textSection.append("    li $v0, 2\n");
            textSection.append("    syscall\n");
        } else if ("char".equalsIgnoreCase(res.type)) {
            textSection.append("    li $v0, 11\n");
            if (!"$a0".equals(res.reg)) {
                textSection.append("    move $a0, ").append(res.reg).append("\n");
            }
            textSection.append("    syscall\n");
        } else if ("string".equalsIgnoreCase(res.type)) {
            textSection.append("    li $v0, 4\n");
            if (!"$a0".equals(res.reg)) {
                textSection.append("    move $a0, ").append(res.reg).append("\n");
            }
            textSection.append("    syscall\n");
        } else {
            // int / boolean
            textSection.append("    li $v0, 1\n");
            if (!"$a0".equals(res.reg)) {
                textSection.append("    move $a0, ").append(res.reg).append("\n");
            }
            textSection.append("    syscall\n");
        }
        printNewLine();
    }

    private void handleCallStmt(CallNode cn) {
        emitCall(cn);
        // Si retorna algoo se ignora
    }

    private void handleDecide(DecideNode dn) {

        String endLbl = newLabel("decide_end");
        List<CaseNode> cases = dn.getCases();
        ASTNode elseBlock = dn.getElseBlock();

        // evaluar cases en orden
        for (int i = 0; i < cases.size(); i++) {
            CaseNode cn = (CaseNode) cases.get(i);
            String nextLbl = (i < cases.size() - 1 || elseBlock != null) ? newLabel("decide_next") : endLbl;

            emitCondBranch(cn.getExpression(), null, nextLbl);
            if (cn.getBlock() != null) {
                emitBlock(cn.getBlock());
            }
            textSection.append("    j ").append(endLbl).append("\n");

            if (nextLbl != endLbl) {
                textSection.append(nextLbl).append(":\n");
            }
        }

        // ningún case se cumplió
        if (elseBlock != null) {
            if (elseBlock instanceof BlockNode) {
                emitBlock((BlockNode) elseBlock);
            }
        }

        textSection.append(endLbl).append(":\n");
    }

    private void handleLoop(LoopNode ln) {
        String loopStart = newLabel("loop_start");
        String loopEnd = newLabel("loop_end");

        textSection.append(loopStart).append(":\n");
        breakStack.push(loopEnd);

        BlockNode body = ln.getBody();
        if (body != null) {
            emitBlock(body);
        }

        ASTNode cond = ln.getExitCondition();
        if (cond != null) {
            emitCondBranch(cond, loopEnd, loopStart);

        } else {
            textSection.append("    j ").append(loopStart).append("\n"); // Unconditional loop
        }

        textSection.append(loopEnd).append(":\n");
        breakStack.pop();
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
        breakStack.push(endLbl);

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
            else {
                // Ejecutar cualquier expresión como paso (ej: ++i)
                emitExprInt(step); // o emitExprFloat si fuera el caso, pero usualmente es int
            }
        }

        textSection.append("    j ").append(startLbl).append("\n");
        textSection.append(endLbl).append(":\n");
        breakStack.pop();
    }

    private void handleBreak(BreakNode bn) {
        if (!breakStack.isEmpty()) {
            textSection.append("    j ").append(breakStack.peek()).append("\n");
        } else {
            textSection.append("    # break fuera de bucle ignorado\n");
        }
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

            String type = getLValueType(target);

            if ("float".equalsIgnoreCase(type)) {
                textSection.append("    li $v0, 6\n");
                textSection.append("    syscall\n"); // result in $f0
                textSection.append("    lw $t1, 0($sp)\n");
                textSection.append("    addi $sp, $sp, 4\n");
                textSection.append("    s.s $f0, 0($t1)\n");
            } else if ("char".equalsIgnoreCase(type)) {
                textSection.append("    li $v0, 12\n");
                textSection.append("    syscall\n"); // result in $v0
                textSection.append("    lw $t1, 0($sp)\n");
                textSection.append("    addi $sp, $sp, 4\n");
                textSection.append("    sw $v0, 0($t1)\n");
            } else {
                // Leer entero syscall 5
                textSection.append("    li $v0, 5\n");
                textSection.append("    syscall\n");
                textSection.append("    lw $t1, 0($sp)\n");
                textSection.append("    addi $sp, $sp, 4\n");
                textSection.append("    sw $v0, 0($t1)\n");
            }
        }
    }

    private void handleReturn(ReturnNode rn) {
        ASTNode expr = rn.getExpression();

        if (expr != null) {
            if ("float".equalsIgnoreCase(currentReturnType)) {
                if (emitExprFloat(expr)) {
                    // Result already in $f0
                }
            } else if ("boolean".equalsIgnoreCase(currentReturnType)) {
                if (emitExprBool(expr)) {
                    textSection.append("    move $v0, $t0\n");
                }
            } else {
                if (emitExprInt(expr)) {
                    textSection.append("    move $v0, $t0\n");
                } else {
                    textSection.append("    li $v0, 0\n");
                }
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

        String type = getNodeType(expr);
        if (type != null && type.toLowerCase().contains("float"))
            return false;

        // literal int/char/bool
        if (expr instanceof LiteralNode) {
            LiteralNode lit = (LiteralNode) expr;
            if (lit.isInt()) {
                textSection.append("    li $t0, ").append(lit.getIntValue()).append("\n");
                return true;
            }
            if (lit.isChar()) {
                textSection.append("    li $t0, ").append(lit.getCharValue()).append("\n");
                return true;
            }
            if ("boolean".equalsIgnoreCase(lit.getType(null))) {
                boolean val = "true".equalsIgnoreCase(lit.getStringValue());
                textSection.append("    li $t0, ").append(val ? 1 : 0).append("\n");
                return true;
            }
            return false;
        }

        RegisterResult res = emitExpr(expr);
        if (res == null)
            return false;

        if ("float".equalsIgnoreCase(res.type)) {
            if (!"$f0".equals(res.reg)) {
                textSection.append("    mov.s $f0, ").append(res.reg).append("\n");
            }
            textSection.append("    cvt.w.s $f0, $f0\n");
            textSection.append("    mfc1 $t0, $f0\n");
        } else {
            if (!"$t0".equals(res.reg)) {
                textSection.append("    move $t0, ").append(res.reg).append("\n");
            }
        }
        return true;
    }

    // =========================
    // Condiciones boolean -> branches directos
    // =========================
    private void emitCondBranch(ASTNode expr, String trueLabel, String falseLabel) {
        if (falseLabel == null)
            throw new IllegalArgumentException("falseLabel null");

        if (expr == null) {
            textSection.append("    j ").append(falseLabel).append("\n");
            return;
        }

        if (expr instanceof BinaryNode) {
            BinaryNode bn = (BinaryNode) expr;
            String op = bn.getOperator();

            if ("&&".equals(op)) {
                String rhsLbl = newLabel("and_rhs");
                emitCondBranch(bn.getLeft(), rhsLbl, falseLabel);
                textSection.append(rhsLbl).append(":\n");
                emitCondBranch(bn.getRight(), trueLabel, falseLabel);
                return;
            }

            if ("||".equals(op)) {
                String rhsLbl = newLabel("or_rhs");
                emitCondBranch(bn.getLeft(), trueLabel, rhsLbl);
                textSection.append(rhsLbl).append(":\n");
                emitCondBranch(bn.getRight(), trueLabel, falseLabel);
                return;
            }
        }

        RegisterResult res = emitExpr(expr);
        if (res == null) {
            textSection.append("    j ").append(falseLabel).append("\n");
            return;
        }

        if ("float".equalsIgnoreCase(res.type)) {
            textSection.append("    cvt.w.s $f0, ").append(res.reg).append("\n");
            textSection.append("    mfc1 $t0, $f0\n");
            res.reg = "$t0";
        }

        if (trueLabel != null) {
            textSection.append("    bne ").append(res.reg).append(", $zero, ").append(trueLabel).append("\n");
            textSection.append("    j ").append(falseLabel).append("\n");
        } else {
            textSection.append("    beq ").append(res.reg).append(", $zero, ").append(falseLabel).append("\n");
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
            textSection.append("    lw $t0, ").append(off).append("($fp)\n");
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
            textSection.append("    sw $t0, ").append(off).append("($fp)\n");
            return;
        }

        String gl = globalLabel.get(name);
        if (gl != null) {
            textSection.append("    sw $t0, ").append(gl).append("\n");
        }
    }

    private void handleArrayInitialization(String arrayName, ArrayLiteralNode aln) {
        List<ASTNode> elements = flattenArrayLiteral(aln);
        if (elements == null || elements.isEmpty())
            return;

        Integer baseOffset = localOffset.get(arrayName);
        if (baseOffset == null) {
            return;
        }

        // Local array
        String type = localType.get(arrayName);
        int stride = "char".equalsIgnoreCase(type) ? 1 : 4;

        for (int i = 0; i < elements.size(); i++) {
            ASTNode elem = elements.get(i);
            if (elem == null)
                continue;

            int elemOffset = baseOffset + (i * stride);
            if ("float".equalsIgnoreCase(type)) {
                if (emitExprFloat(elem)) {
                    textSection.append("    s.s $f0, ").append(elemOffset).append("($fp)\n");
                }
            } else {
                if (emitExprInt(elem)) {
                    if (stride == 1) {
                        textSection.append("    sb $t0, ").append(elemOffset).append("($fp)\n");
                    } else {
                        textSection.append("    sw $t0, ").append(elemOffset).append("($fp)\n");
                    }
                }
            }
        }
    }

    private List<ASTNode> flattenArrayLiteral(ArrayLiteralNode aln) {
        List<ASTNode> result = new ArrayList<>();
        if (aln == null || aln.getElements() == null)
            return result;
        for (ASTNode n : aln.getElements()) {
            if (n instanceof ArrayLiteralNode) {
                result.addAll(flattenArrayLiteral((ArrayLiteralNode) n));
            } else {
                result.add(n);
            }
        }
        return result;
    }

    private String getNodeType(ASTNode n) {
        if (n == null)
            return "int";

        // DEBUG PRINT
        // System.err.println("TRACE: getNodeType(" + n.getClass().getSimpleName() +
        // ")");

        // 1. VariableNode: check maps directly
        if (n instanceof VariableNode) {
            String name = ((VariableNode) n).getName();
            if (localType.containsKey(name)) {
                // System.err.println(" VAR " + name + " -> local: " + localType.get(name));
                return localType.get(name);
            }
            // System.err.println(" VAR " + name + " -> global/default: " +
            // globalType.getOrDefault(name, "int"));
            return globalType.getOrDefault(name, "int");
        }
        if (n instanceof UnaryNode) {
            String t = getNodeType(((UnaryNode) n).getExpression());
            // System.err.println(" UNARY peel -> " + t);
            return t;
        }
        if (n instanceof BinaryNode) {
            String t1 = getNodeType(((BinaryNode) n).getLeft());
            String t2 = getNodeType(((BinaryNode) n).getRight());
            if ("float".equalsIgnoreCase(t1) || "float".equalsIgnoreCase(t2))
                return "float";
            return "int";
        }

        if (isNodeNamed(n, "ArrayAccessNode")) {
            return getLValueType(n);
        }
        String t = n.getType((semantics.SymbolTable) semTab);
        if (t != null && t.toLowerCase().contains("float"))
            return "float";
        return t;
    }

    private boolean emitBoolToT0(ASTNode expr) {
        String lblTrue = newLabel("bool_t");
        String lblFalse = newLabel("bool_f");
        String lblEnd = newLabel("bool_end");

        try {
            emitCondBranch(expr, lblTrue, lblFalse);
        } catch (Exception e) {
            return false;
        }

        // False block
        textSection.append(lblFalse).append(":\n");
        textSection.append("    li $t0, 0\n");
        textSection.append("    j ").append(lblEnd).append("\n");

        // True block
        textSection.append(lblTrue).append(":\n");
        textSection.append("    li $t0, 1\n");

        // End
        textSection.append(lblEnd).append(":\n");
        return true;
    }

    private boolean emitExprBool(ASTNode expr) {
        RegisterResult res = emitExpr(expr);
        if (res == null)
            return false;
        if (!"$t0".equals(res.reg)) {
            textSection.append("    move $t0, ").append(res.reg).append("\n");
        }
        return true;
    }

    private boolean isNodeNamed(Object o, String simpleName) {
        return o != null && o.getClass().getSimpleName().equals(simpleName);
    }

    private Object callNoArg(Object o, String method) {
        if (o == null)
            return null;
        try {
            java.lang.reflect.Method m = o.getClass().getMethod(method);
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
                textSection.append("    addi $t1, $fp, ").append(off).append("\n");
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
                textSection.append("    addi $t1, $fp, ").append(off).append("\n");
            } else {
                String gl = globalLabel.get(arrName);
                if (gl == null) {
                    textSection.append("    li $t1, 0\n");
                    return false;
                }
                textSection.append("    la $t1, ").append(gl).append("\n");
            }

            // [FIX] Guardar base ($t1) en pila porque emitExprInt puede usar $t1
            textSection.append("    addi $sp, $sp, -4\n");
            textSection.append("    sw $t1, 0($sp)\n");

            // dims para 2D
            List<Integer> dims = (off != null) ? localDims.get(arrName) : globalDims.get(arrName);
            int cols = -1;
            if (dims != null && dims.size() >= 2 && dims.get(1) != null) {
                cols = dims.get(1);
            }

            // si es acceso 2D y no hay cols válido, abortar
            if (idx.size() >= 2 && cols <= 0) {
                textSection.append("    addi $sp, $sp, 4\n"); // pop base
                textSection.append("    li $t1, 0\n");
                return false;
            }

            // idx0
            if (!emitExprInt(idx.get(0))) {
                textSection.append("    addi $sp, $sp, 4\n"); // pop base
                return false;
            }
            // resultado idx0 en $t0
            textSection.append("    move $t2, $t0\n");

            if (idx.size() >= 2) {
                // [FIX] Guardar idx0 ($t2) en pila porque emitExprInt puede usar $t2
                textSection.append("    addi $sp, $sp, -4\n");
                textSection.append("    sw $t2, 0($sp)\n");

                if (!emitExprInt(idx.get(1))) {
                    textSection.append("    addi $sp, $sp, 8\n"); // pop idx0 + base
                    return false;
                }
                textSection.append("    move $t3, $t0\n");

                // Recuperar idx0
                textSection.append("    lw $t2, 0($sp)\n");
                textSection.append("    addi $sp, $sp, 4\n");

                // t2 = t2*cols + t3
                textSection.append("    li $t4, ").append(cols).append("\n");
                textSection.append("    mul $t2, $t2, $t4\n");
                textSection.append("    addu $t2, $t2, $t3\n");
            }

            textSection.append("    addi $sp, $sp, -4\n");
            textSection.append("    sw $t2, 0($sp)\n");

            // Recuperar base ($t1)
            textSection.append("    lw $t1, 4($sp)\n");

            // byte offset = linear * stride
            String type = getLValueType(target);

            // Recuperar linear ($t2)
            textSection.append("    lw $t2, 0($sp)\n");
            textSection.append("    addi $sp, $sp, 8\n"); // Pop t2 y base

            if (!"char".equalsIgnoreCase(type)) {
                textSection.append("    sll $t2, $t2, 2\n");
            }
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
        currentReturnType = fn.getReturnType();

        textSection.append("\n").append(fname).append(":\n");

        textSection.append("    addi $sp, $sp, -4\n");
        textSection.append("    sw $fp, 0($sp)\n");
        textSection.append("    move $fp, $sp\n");
        textSection.append("    addi $sp, $sp, -4\n");
        textSection.append("    sw $ra, 0($sp)\n");

        localOffset.clear();
        localType.clear();
        localDims.clear();
        localBytes = 0;

        BlockNode body = fn.getBlock();
        List<ParamNode> ps = fn.getParams();

        if (body != null) {
            preScanLocals(body);
        }

        if (localBytes > 0) {
            textSection.append("    addi $sp, $sp, -").append(localBytes).append("\n");
        }

        if (ps != null) {
            for (int i = 0; i < ps.size(); i++) {
                ParamNode p = ps.get(i);
                localType.put(p.getName(), p.getType());
                localOffset.put(p.getName(), 4 + (i * 4));
            }
        }

        String prevExit = currentExitLabel;
        int allocBytes = (localBytes + 7) & ~7;
        textSection.append("    addi $sp, $sp, -").append(allocBytes).append("\n");

        currentExitLabel = exitLbl;

        if (body != null)
            emitBlock(body);

        textSection.append(exitLbl).append(":\n");
        textSection.append("    lw $ra, -4($fp)\n");
        textSection.append("    move $sp, $fp\n");

        textSection.append("    lw $fp, 0($sp)\n");
        textSection.append("    addi $sp, $sp, 4\n");

        textSection.append("    jr $ra\n");

        currentExitLabel = prevExit;
    }

    private void storeVarFromFloat(String name) {
        Integer off = localOffset.get(name);
        if (off != null) {
            textSection.append("    s.s $f0, ").append(off).append("($fp)\n");
            return;
        }
        String gl = globalLabel.get(name);
        if (gl != null) {
            textSection.append("    s.s $f0, ").append(gl).append("\n");
        }
    }

    private void loadVarToFloat(String name) {
        Integer off = localOffset.get(name);
        if (off != null) {
            textSection.append("    l.s $f0, ").append(off).append("($fp)\n");
            return;
        }
        String gl = globalLabel.get(name);
        if (gl != null) {
            textSection.append("    l.s $f0, ").append(gl).append("\n");
            return;
        }
        textSection.append("    mtc1 $zero, $f0\n");
        textSection.append("    cvt.s.w $f0, $f0\n");
    }

    private String getLValueType(ASTNode target) {
        if (target instanceof VariableNode) {
            String name = ((VariableNode) target).getName();
            if (localType.containsKey(name))
                return localType.get(name);
            if (globalType.containsKey(name))
                return globalType.get(name);
        }
        if (isNodeNamed(target, "ArrayAccessNode")) {
            ASTNode base = (ASTNode) callNoArg(target, "getArray");
            if (base == null)
                base = (ASTNode) callNoArg(target, "getBase");
            if (base instanceof VariableNode) {
                String name = ((VariableNode) base).getName();
                if (localType.containsKey(name))
                    return localType.get(name);
                if (globalType.containsKey(name))
                    return globalType.get(name);
            }
        }
        return "int";
    }

    private String internFloat(float f) {
        String label = "flt_" + (labelCounter++);
        dataSection.add(label + ": .float " + f);
        return label;
    }

    private boolean emitExprFloat(ASTNode expr) {
        RegisterResult res = emitExpr(expr);
        if (res == null)
            return false;

        if ("float".equalsIgnoreCase(res.type)) {
            if (!"$f0".equals(res.reg)) {
                textSection.append("    mov.s $f0, ").append(res.reg).append("\n");
            }
        } else {
            if (!"$t0".equals(res.reg)) {
                textSection.append("    move $t0, ").append(res.reg).append("\n");
            }
            textSection.append("    mtc1 $t0, $f0\n");
            textSection.append("    cvt.s.w $f0, $f0\n");
        }
        return true;
    }

    private void emitCall(CallNode cn) {
        String fname = cn.getName();
        List<ASTNode> args = cn.getArguments();
        int n = (args == null) ? 0 : args.size();

        for (int i = n - 1; i >= 0; i--) {
            ASTNode a = args.get(i);
            String type = getNodeType(a);
            if ("float".equalsIgnoreCase(type)) {
                if (emitExprFloat(a)) {
                    textSection.append("    addi $sp, $sp, -4\n");
                    textSection.append("    swc1 $f0, 0($sp)\n");
                } else {
                    textSection.append("    addi $sp, $sp, -4\n");
                    textSection.append("    sw $zero, 0($sp)\n");
                }
            } else {
                if (emitExprInt(a)) {
                    textSection.append("    addi $sp, $sp, -4\n");
                    textSection.append("    sw $t0, 0($sp)\n");
                } else {
                    textSection.append("    addi $sp, $sp, -4\n");
                    textSection.append("    sw $zero, 0($sp)\n");
                }
            }
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

        Files.write(java.nio.file.Paths.get(rutaMips), asm.toString().getBytes(StandardCharsets.UTF_8));
    }

}
