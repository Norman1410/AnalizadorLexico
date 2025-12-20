import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.lang.reflect.Field;

import java_cup.runtime.Symbol;

public class Main {

    public static void main(String[] args) {
        // El programa se ejecuta con: java Main <ruta_archivo>
        // Si no me pasan la ruta, no puedo leer nada.
        if (args.length == 0) {
            System.out.println("Uso: java Main <ruta_del_archivo>");
            return;
        }

        String rutaEntrada = args[0];
        String rutaSalida = "tokens_salida.txt";

        try (Reader reader = new BufferedReader(new FileReader(rutaEntrada));
             PrintWriter out = new PrintWriter(new FileWriter(rutaSalida))) {

            // Scanner generado por JFlex (renombrado para no chocar con java.util.Scanner).
            LexicoScanner lexer = new LexicoScanner(reader);

            while (true) {
                Symbol s = lexer.next_token();

                int idToken = s.sym;
                String nombreToken = nombreDeToken(idToken);

                // El lexema normalmente lo guardamos en s.value.
                // Si viene null (por ejemplo en EOF), lo dejamos vacío.
                String lexema = (s.value == null) ? "" : String.valueOf(s.value);

                // En nuestro .flex usamos left/right como línea/columna.
                out.printf("TOKEN=%s  LEXEMA=%s  (linea=%d,col=%d)%n",
                        nombreToken, lexema, s.left, s.right);

                if (idToken == sym.EOF) {
                    break;
                }
            }

            System.out.println("Listo. Tokens guardados en " + rutaSalida);

        } catch (Exception e) {
            System.err.println("Error ejecutando el analizador léxico: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Convierte el id numérico del token a su nombre (por ejemplo: 3 -> WORLD).
    // Así no tengo que escribir un switch gigante a mano.
    private static String nombreDeToken(int id) {
        try {
            Field[] campos = sym.class.getFields();
            for (Field f : campos) {
                if (f.getType() == int.class) {
                    int valor = f.getInt(null);
                    if (valor == id) {
                        return f.getName();
                    }
                }
            }
        } catch (Exception e) {
            // Si falla, no pasa nada: devolvemos el id.
        }
        return String.valueOf(id);
    }
}
