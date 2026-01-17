package semantics;

import java.util.*;

public class SymbolInfo {
    public final String name;
    public final String type;
    public final SymbolKind kind;
    public final int line;
    public final int col;
    public final List<Integer> dims;

    public SymbolInfo(String name, String type, SymbolKind kind, int line, int col, List<Integer> dims) {
        this.name = name;
        this.type = type;
        this.kind = kind;
        this.line = line;
        this.col = col;
        this.dims = (dims == null) ? new ArrayList<>() : dims;
    }

    @Override
    public String toString() {
        String d = dims.isEmpty() ? "" : (" dims=" + dims);
        return kind + " " + name + " : " + type + " (line=" + (line + 1) + ", col=" + (col + 1) + ")" + d;
    }
}
