import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.lang.reflect.Field;

import java_cup.runtime.Symbol;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        // Se ejecuta así:
        // java Main <ruta_archivo>
        if (args.length == 0) {
            System.out.println("Uso: java Main <ruta_del_archivo>");
            return;
        }

        String rutaEntrada = args[0];
        String rutaSalida = "tokens_salida.txt";

        try (Reader reader = new BufferedReader(new FileReader(rutaEntrada));
                PrintWriter out = new PrintWriter(new FileWriter(rutaSalida))) {

            // Scanner generado por JFlex
            LexicoScanner lexer = new LexicoScanner(reader);

            while (true) {
                Symbol s = lexer.next_token();

                int idToken = s.sym;
                String nombreToken = nombreDeToken(idToken);

                // El lexema viene en s.value cuando el scanner lo manda.
                String lexema = (s.value == null) ? "" : String.valueOf(s.value);

                // Reporte normal de token
                out.printf("ID=%d  TOKEN=%s  LEXEMA=%s  (linea=%d,col=%d)%n",
                        idToken, nombreToken, lexema, s.left, s.right);

                // Si el token es ERROR, lo marco también por consola para enterarme rápido.
                // (El programa no se detiene, solo lo reporta.)
                if (idToken == sym.ERROR) {
                    System.err.printf("Advertencia léxica en linea %d, col %d -> %s%n",
                            s.left, s.right, lexema);
                }

                if (idToken == sym.EOF) {
                    break;
                }
            }

            System.out.println("Listo. Tokens guardados en " + rutaSalida);

            try (Reader reader2 = new BufferedReader(new FileReader(rutaEntrada))) {
                LexicoScanner lexer2 = new LexicoScanner(reader2);
                Parser parser = new Parser(lexer2);

                Symbol root = parser.parse();
                ast.ProgramNode program = (ast.ProgramNode) root.value;

                System.out.println("\nParser: el archivo SI puede ser generado por la gramática.");
                System.out.println("=== ÁRBOL SINTÁCTICO GENERADO ===");
                if (program != null) {
                    program.print(0);

                    // Exportar a JSON para el visualizador web
                    Map<String, Object> astJson = program.toJsonObject();
                    String jsonString = toJsonString(astJson);
                    try (FileWriter writer = new FileWriter("ast_output.json")) {
                        writer.write(jsonString);
                        System.out.println("\nAST exportado con éxito a 'ast_output.json'");
                    }
                }
            } catch (Exception ex) {
                System.out.println("\nParser: el archivo NO puede ser generado por la gramática.");
                System.out.println("Detalle: " + ex.getMessage());
                // ex.printStackTrace(); // Para debugging profundo
            }

        } catch (Exception e) {
            System.err.println("Error ejecutando el analizador léxico: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Convierte el id numérico del token a su nombre (ej: 3 -> WORLD).
    // Así no tengo que mantener un switch enorme a mano.
    private static String nombreDeToken(int id) {
        // ... (código existente)
        return String.valueOf(id);
    }

    /**
     * Serializador JSON simple y recursivo para evitar dependencias externas.
     */
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
            for (int i = 0; i < list.size(); i++) {
                sb.append(toJsonString(list.get(i)));
                if (i < list.size() - 1)
                    sb.append(",");
            }
            sb.append("]");
            return sb.toString();
        }

        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            StringBuilder sb = new StringBuilder("{");
            int i = 0;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                sb.append("\"").append(entry.getKey()).append("\":");
                sb.append(toJsonString(entry.getValue()));
                if (i < map.size() - 1)
                    sb.append(",");
                i++;
            }
            sb.append("}");
            return sb.toString();
        }

        return "\"" + obj.toString() + "\"";
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
