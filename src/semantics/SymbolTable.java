package semantics;

import java.util.*;

public class SymbolTable {

    // Pila de scopes (cada scope tiene su propio mapa name -> SymbolInfo)
    private final Deque<Map<String, SymbolInfo>> scopes = new ArrayDeque<>();
    private final Deque<String> scopeNames = new ArrayDeque<>();
    // Scopes ya cerrados (para poder imprimirlos luego)
    private final List<String> closedScopeNames = new ArrayList<>();
    private final List<Map<String, SymbolInfo>> closedScopes = new ArrayList<>();

    private final List<String> errors = new ArrayList<>();

    public SymbolTable() {
        // opcional: arrancar con global listo
        enterScope("global");
    }

    public void enterScope(String name) {
        scopeNames.push(name == null ? "scope" : name);
        scopes.push(new LinkedHashMap<>());
    }

    public void exitScope() {
        if (!scopes.isEmpty() && !scopeNames.isEmpty()) {
            Map<String, SymbolInfo> closed = scopes.pop();
            String closedName = scopeNames.pop();

            // Guardar el scope que se cerró para imprimirlo después
            closedScopes.add(closed);
            closedScopeNames.add(closedName);
        }
    }


    public String currentScope() {
        return scopeNames.isEmpty() ? "global" : scopeNames.peek();
    }

    public void declare(SymbolInfo info) {
        if (info == null || info.name == null) return;

        if (scopes.isEmpty()) enterScope("global");
        Map<String, SymbolInfo> current = scopes.peek();

        if (current.containsKey(info.name)) {
            SymbolInfo prev = current.get(info.name);
            errors.add("Redeclaración de '" + info.name + "' en scope '" + currentScope()
                    + "'. Ya existe como " + prev.kind
                    + " (line=" + prev.line + ", col=" + prev.col + ")");
            return;
        }

        current.put(info.name, info);
    }

    /** Busca de adentro hacia afuera */
    public SymbolInfo lookup(String name) {
        if (name == null) return null;
        for (Map<String, SymbolInfo> s : scopes) {
            if (s.containsKey(name)) return s.get(name);
        }
        return null;
    }

    /** Devuelve toodo con clave calificada por scope para poder imprimirlo */
    public Map<String, SymbolInfo> getAll() {
        Map<String, SymbolInfo> out = new LinkedHashMap<>();

        // 1) Primero imprimir scopes que ya se cerraron (en orden de cierre)
        for (int i = 0; i < closedScopes.size(); i++) {
            String scName = closedScopeNames.get(i);
            Map<String, SymbolInfo> sc = closedScopes.get(i);
            for (SymbolInfo si : sc.values()) {
                out.put(scName + "::" + si.name, si);
            }
        }

        // 2) Luego los scopes que siguen abiertos (ej: global)
        List<Map<String, SymbolInfo>> list = new ArrayList<>(scopes);
        List<String> names = new ArrayList<>(scopeNames);
        Collections.reverse(list);
        Collections.reverse(names);

        for (int i = 0; i < list.size(); i++) {
            String scName = names.get(i);
            for (SymbolInfo si : list.get(i).values()) {
                out.put(scName + "::" + si.name, si);
            }
        }

        return out;
    }


    public List<String> getErrors() {
        return errors;
    }

    public String toPrettyString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== TABLA DE SÍMBOLOS ===\n");

        for (Map.Entry<String, SymbolInfo> e : getAll().entrySet()) {
            sb.append(e.getKey()).append(" -> ").append(e.getValue()).append("\n");
        }

        if (!errors.isEmpty()) {
            sb.append("\n=== ERRORES SEMÁNTICOS ===\n");
            for (String er : errors) sb.append(er).append("\n");
        }
        return sb.toString();
    }

    public String toPrettyStringByScope() {
        StringBuilder sb = new StringBuilder();

        // 1) Scopes cerrados primero (funciones, etc.)
        for (int i = 0; i < closedScopes.size(); i++) {
            String scName = closedScopeNames.get(i);
            sb.append("\nTabla de símbolo : ").append(scName).append("\n");
            sb.append("Valores:\n");
            for (SymbolInfo si : closedScopes.get(i).values()) {
                sb.append(si.kind)
                        .append(" ")
                        .append(si.name)
                        .append(" : ")
                        .append(si.type)
                        .append(" (line=")
                        .append(si.line)
                        .append(", col=")
                        .append(si.col)
                        .append(")");

                if (si.dims != null && !si.dims.isEmpty()) {
                    sb.append(" dims=").append(si.dims);
                }

                sb.append("\n");
            }
        }

        // 2) Scopes abiertos al final (global, main si no lo cerraste)
        List<Map<String, SymbolInfo>> list = new ArrayList<>(scopes);
        List<String> names = new ArrayList<>(scopeNames);
        Collections.reverse(list);
        Collections.reverse(names);

        for (int i = 0; i < list.size(); i++) {
            String scName = names.get(i);
            sb.append("\nTabla de símbolo : ").append(scName).append("\n");
            sb.append("Valores:\n");
            for (SymbolInfo si : list.get(i).values()) {
                sb.append(si.kind)
                        .append(" ")
                        .append(si.name)
                        .append(" : ")
                        .append(si.type)
                        .append(" (line=")
                        .append(si.line)
                        .append(", col=")
                        .append(si.col)
                        .append(")");

                if (si.dims != null && !si.dims.isEmpty()) {
                    sb.append(" dims=").append(si.dims);
                }

                sb.append("\n");
            }
        }

        return sb.toString();
    }



}
