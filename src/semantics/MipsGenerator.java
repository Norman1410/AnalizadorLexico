package semantics;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import ast.*;

public class MipsGenerator {

    private final Object semTab; // luego lo usamos para tipos/offsets “pro”, por ahora no estorba

    // Secciones del .asm
    private final List<String> dataSection = new ArrayList<>();
    private final StringBuilder textSection = new StringBuilder();

    // Pool de strings para no duplicar labels
    private int strCounter = 0;
    private final Map<String, String> stringPool = new HashMap<>();

    // “Memoria” local de variables en main (offset desde $s0)
    private final Map<String, Integer> localOffset = new HashMap<>();
    private final Map<String, String> localType = new HashMap<>();
    private int localBytes = 0;

    public MipsGenerator(Object semTab) {
        this.semTab = semTab;
    }

    public void generate(ProgramNode program, String rutaMips) throws IOException {
        if (program == null) throw new IllegalArgumentException("program null");
        if (rutaMips == null || rutaMips.isBlank()) throw new IllegalArgumentException("rutaMips vacía");

        // reset por si corrés varias veces
        dataSection.clear();
        textSection.setLength(0);
        stringPool.clear();
        localOffset.clear();
        localType.clear();
        localBytes = 0;
        strCounter = 0;

        // =====================
        // DATA base
        // =====================
        dataSection.add(".data");
        dataSection.add("nl: .asciiz \"\\n\"");

        // =====================
        // TEXT base
        // =====================
        textSection.append(".text\n");
        textSection.append(".globl main\n\n");
        textSection.append("main:\n");

        // Tomamos el mainBlock (en tu AST puede ser MainNode o directamente BlockNode)
        ASTNode mainAst = program.getMainBlock();
        BlockNode mainBlock = null;

        if (mainAst instanceof MainNode mn) {
            ASTNode blk = mn.getBlock();
            if (blk instanceof BlockNode b) mainBlock = b;
        } else if (mainAst instanceof BlockNode b) {
            mainBlock = b;
        }

        if (mainBlock == null) {
            // main raro: salimos sin romper
            textSection.append("    li $v0, 10\n");
            textSection.append("    syscall\n");
            writeAsm(rutaMips);
            return;
        }

        // 1) reservar espacio para locals
        preScanLocals(mainBlock);

        // prolog stack
        if (localBytes > 0) {
            textSection.append("    addi $sp, $sp, -").append(localBytes).append("\n");
            textSection.append("    move $s0, $sp\n"); // base locals
        }

        // 2) emitir statements del main
        emitBlock(mainBlock);

        // epílogo stack
        if (localBytes > 0) {
            textSection.append("    addi $sp, $sp, ").append(localBytes).append("\n");
        }

        // exit
        textSection.append("    li $v0, 10\n");
        textSection.append("    syscall\n");

        writeAsm(rutaMips);
    }

    // -------------------------
    // Pre-scan locals
    // -------------------------
    private void preScanLocals(BlockNode block) {
        List<ASTNode> stmts = block.getStatements();
        if (stmts == null) return;

        int offset = 0;
        for (ASTNode s : stmts) {
            if (s instanceof DeclNode dn && !dn.isGlobal()) {
                localOffset.put(dn.getName(), offset);
                localType.put(dn.getName(), dn.getTypeName()); // "int" / "string" / ...
                offset += 4; // por ahora todo word
            }
        }
        localBytes = offset;
    }

    // -------------------------
    // Emit block
    // -------------------------
    private void emitBlock(BlockNode block) {
        List<ASTNode> stmts = block.getStatements();
        if (stmts == null) return;

        for (ASTNode s : stmts) {
            if (s == null) continue;

            if (s instanceof DeclNode dn) {
                handleDecl(dn);
            } else if (s instanceof AssignNode an) {
                handleAssign(an);
            } else if (s instanceof ShowNode sn) {
                handleShow(sn);
            } else if (s instanceof ReturnNode) {
                // main es void en tu semántica: return solo termina. No imprimimos nada.
                // (igual el exit lo ponemos al final)
            } else {
                // todavía no soportado (if/loop/for...)
                textSection.append("    # ignorado: ").append(s.getClass().getSimpleName()).append("\n");
            }
        }
    }

    // -------------------------
    // DECL local
    // -------------------------
    private void handleDecl(DeclNode dn) {
        if (dn.isGlobal()) return;

        ASTNode init = dn.getInitializer();
        if (init == null) return;

        if ("int".equals(dn.getTypeName())) {
            if (emitExprInt(init)) {
                storeVarFromT0(dn.getName());
            }
            return;
        }

        if ("string".equals(dn.getTypeName()) && init instanceof LiteralNode lit && lit.isString()) {
            String label = internString(lit.getStringValue());
            textSection.append("    la $t0, ").append(label).append("\n");
            storeVarFromT0(dn.getName());
        }
    }

    // -------------------------
    // ASSIGN (solo variable por ahora)
    // -------------------------
    private void handleAssign(AssignNode an) {
        ASTNode target = an.getTarget();
        ASTNode expr = an.getExpression();

        if (!(target instanceof VariableNode tv)) return;
        String dst = tv.getName();

        String dstType = localType.getOrDefault(dst, "int");

        if ("int".equalsIgnoreCase(dstType)) {
            if (emitExprInt(expr)) {
                storeVarFromT0(dst);
            }
            return;
        }

        if ("string".equalsIgnoreCase(dstType)) {
            if (expr instanceof LiteralNode lit && lit.isString()) {
                String label = internString(lit.getStringValue());
                textSection.append("    la $t0, ").append(label).append("\n");
                storeVarFromT0(dst);
                return;
            }

            if (expr instanceof VariableNode vn) {
                loadVarToT0(vn.getName());
                storeVarFromT0(dst);
                return;
            }

            // Si cae aquí: no soportado (ej: string = expr rara)
            return;
        }

    }

    // -------------------------
    // SHOW
    // -------------------------
    private void handleShow(ShowNode show) {
        ASTNode expr = show.getExpression();

        // 1) string literal
        if (expr instanceof LiteralNode lit && lit.isString()) {
            String label = internString(lit.getStringValue());
            printStringLabel(label);
            printNewLine();
            return;
        }

        // 2) int literal
        if (expr instanceof LiteralNode lit && lit.isInt()) {
            printIntImm(lit.getIntValue());
            printNewLine();
            return;
        }

        // 3) expresión int (variable, binary, unary, etc.)
        if (emitExprInt(expr)) {          // deja resultado en $t0
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


    // =========================
    // Expresiones int -> resultado en $t0
    // =========================
    private boolean emitExprInt(ASTNode expr) {
        if (expr == null) return false;

        // literal int
        if (expr instanceof LiteralNode lit) {
            if (!lit.isInt()) return false;
            textSection.append("    li $t0, ").append(lit.getIntValue()).append("\n");
            return true;
        }


        // variable int
        if (expr instanceof VariableNode vn) {
            loadVarToT0(vn.getName());
            return true;
        }

        // unary
        if (expr instanceof UnaryNode un) {
            if (!emitExprInt(un.getExpression())) return false;

            String op = un.getOperator();
            if ("-".equals(op) || "neg".equalsIgnoreCase(op)) {
                textSection.append("    sub $t0, $zero, $t0\n");
                return true;
            }
            return false;
        }

        // binary
        if (expr instanceof BinaryNode bn) {
            String op = bn.getOperator();

            if (!emitExprInt(bn.getLeft())) return false;

            // push left
            textSection.append("    addi $sp, $sp, -4\n");
            textSection.append("    sw $t0, 0($sp)\n");

            if (!emitExprInt(bn.getRight())) return false;

            // pop left -> $t1
            textSection.append("    lw $t1, 0($sp)\n");
            textSection.append("    addi $sp, $sp, 4\n");

            switch (op) {
                case "+" -> textSection.append("    add $t0, $t1, $t0\n");
                case "-" -> textSection.append("    sub $t0, $t1, $t0\n");
                case "*" -> textSection.append("    mul $t0, $t1, $t0\n");
                case "/" , "//" -> {
                    textSection.append("    div $t1, $t0\n");
                    textSection.append("    mflo $t0\n");
                }
                case "%" -> {
                    textSection.append("    div $t1, $t0\n");
                    textSection.append("    mfhi $t0\n");
                }
                default -> { return false; }
            }
            return true;
        }

        return false;
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
        if (off == null) {
            textSection.append("    li $t0, 0\n");
            return;
        }
        textSection.append("    lw $t0, ").append(off).append("($s0)\n");
    }

    private void storeVarFromT0(String name) {
        Integer off = localOffset.get(name);
        if (off == null) return;
        textSection.append("    sw $t0, ").append(off).append("($s0)\n");
    }

    // =========================
    // Strings
    // =========================
    private String internString(String raw) {
        if (raw == null) raw = "";
        if (stringPool.containsKey(raw)) return stringPool.get(raw);

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
    // Literal parsing (tu LiteralNode guarda value privado)
    // =========================
    private int parseIntLiteral(LiteralNode lit) {
        Object v = getField(lit, "value");
        if (v instanceof Integer i) return i;
        if (v instanceof String s) {
            try { return Integer.parseInt(s.replace("\"", "").trim()); } catch (Exception ignored) {}
        }
        return 0;
    }

    private String parseStringLiteral(LiteralNode lit) {
        Object v = getField(lit, "value");
        if (v instanceof String s) {
            if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
                return s.substring(1, s.length() - 1);
            }
            return s;
        }
        return "";
    }

    private Object getField(Object obj, String fieldName) {
        if (obj == null) return null;
        try {
            Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(obj);
        } catch (Exception e) {
            return null;
        }
    }

    private void writeAsm(String rutaMips) throws IOException {
        StringBuilder asm = new StringBuilder();
        for (String line : dataSection) asm.append(line).append("\n");
        asm.append("\n");
        asm.append(textSection);

        Files.writeString(Path.of(rutaMips), asm.toString(), StandardCharsets.UTF_8);
    }
}
