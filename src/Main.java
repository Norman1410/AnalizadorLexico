import java.io.*;
import java_cup.runtime.Symbol;
import semantics.SymbolTable;
import ast.ASTNode;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        String rutaEntrada;
        if (args.length < 1) {
            rutaEntrada = "src/test.txt";
            System.out.println("No se especificó archivo de entrada. Usando por defecto: " + rutaEntrada);
        } else {
            rutaEntrada = args[0];
        }
        String rutaSalida = "tokens_salida.txt";

        try {
            // Análisis Léxico
            try (Reader reader = new BufferedReader(new FileReader(rutaEntrada));
                    PrintWriter writer = new PrintWriter(new FileWriter(rutaSalida))) {

                LexicoScanner lexer = new LexicoScanner(reader);
                while (true) {
                    Symbol s = lexer.next_token();
                    int idToken = s.sym;
                    String nombreToken = nombreDeToken(idToken);
                    String lexema = (s.value != null) ? s.value.toString() : "";

                    if (idToken != sym.EOF) {
                        writer.printf("ID: %-3d  Token: %-15s  Lexema: %-20s  Línea: %d  Columna: %d%n",
                                idToken, nombreToken, lexema, s.left + 1, s.right + 1);
                    } else {
                        break;
                    }
                }
            }
            System.out.println("Tokens guardados en " + rutaSalida);

            try (Reader reader2 = new BufferedReader(new FileReader(rutaEntrada))) {
                LexicoScanner lexer2 = new LexicoScanner(reader2);
                Parser parser = new Parser(lexer2);
                Symbol root = parser.parse();

                if (parser.getSyntaxErrors() == 0) {
                    System.out.println("\nParser: el archivo si puede ser generado por la gramática.");
                } else {
                    System.out.println("\nParser: el archivo no puede ser generado por la gramática.");
                    System.out.println("Cantidad de errores sintácticos: " + parser.getSyntaxErrors());
                }

                if (root != null && root.value instanceof ast.ProgramNode) {
                    ast.ProgramNode program = (ast.ProgramNode) root.value;

                    System.out.println("ÁRBOL SINTÁCTICO GENERADO");
                    program.print(0);

                    // Tabla de símbolos
                    SymbolTable symtab = program.buildSymbolTable();
                    program.validateArraySemantics(symtab);
                    System.out.println("\nTABLA DE SÍMBOLOS");
                    System.out.println(symtab.toPrettyStringByScope());

                    try (PrintWriter stWriter = new PrintWriter(new FileWriter("symbol_table.txt"))) {
                        stWriter.print(symtab.toPrettyStringByScope());
                    }
                    System.out.println("Tabla de símbolos exportada a 'symbol_table.txt'");

                    // Exportar AST a JSON
                    Map<String, Object> astJson = program.toJsonObject();
                    String jsonString = toJsonString(astJson);

                    try (FileWriter writer = new FileWriter("ast_output.json")) {
                        writer.write(jsonString);
                        System.out.println("\nAST exportado con éxito a 'ast_output.json'");
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error ejecutando el proceso: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String nombreDeToken(int id) {
        try {
            java.lang.reflect.Field[] fields = sym.class.getFields();
            for (java.lang.reflect.Field field : fields) {
                if (field.getInt(null) == id) {
                    return field.getName();
                }
            }
        } catch (Exception e) {
            return "UNKNOWN";
        }
        return "UNKNOWN";
    }

    private static String toJsonString(Object obj) {
        if (obj == null)
            return "null";

        if (obj instanceof String)
            return "\"" + escapeJson((String) obj) + "\"";
        if (obj instanceof Number || obj instanceof Boolean)
            return obj.toString();

        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : list) {
                if (item == null)
                    continue;
                if (!first)
                    sb.append(",");
                sb.append(toJsonString(item));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }

        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getValue() == null)
                    continue;
                if (!first)
                    sb.append(",");
                sb.append("\"").append(entry.getKey()).append("\":");
                sb.append(toJsonString(entry.getValue()));
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }

        return "\"" + obj.toString() + "\"";
    }

    private static String escapeJson(String input) {
        if (input == null)
            return null;
        return input.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
