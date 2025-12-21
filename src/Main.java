import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.lang.reflect.Field;

import java_cup.runtime.Symbol;

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

        } catch (Exception e) {
            System.err.println("Error ejecutando el analizador léxico: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Convierte el id numérico del token a su nombre (ej: 3 -> WORLD).
    // Así no tengo que mantener un switch enorme a mano.
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
            // Si falla, devuelvo el id como texto y ya.
        }
        return String.valueOf(id);
    }
}
