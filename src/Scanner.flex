import java_cup.runtime.Symbol;

%%

%public
%class LexicoScanner
%unicode
%cup
%line
%column

%state COMMENT

/* Helpers para crear tokens */
%{
  private Symbol tok(int type) {
    return new Symbol(type, yyline + 1, yycolumn + 1, yytext());
  }

  private Symbol tok(int type, Object value) {
    return new Symbol(type, yyline + 1, yycolumn + 1, value);
  }
%}

/* =========================
   MACROS (DEFINICIONES)
   ========================= */
WHITE      = [ \t\r\n]+
DIGIT      = [0-9]
LETTER     = [A-Za-z_]
IDENT      = {LETTER}({LETTER}|{DIGIT})*
INT        = {DIGIT}+
FLOAT      = {DIGIT}+ "." {DIGIT}+
ESC        = \\[nrt\\\"']
CHAR       = \'([^\\'\r\n]|{ESC})\'
STRING     = \"([^\\\"\r\n]|{ESC})*\"

%%

/* =========================
   REGLAS
   ========================= */

/* Ignorar espacios y saltos */
{WHITE}        { /* se ignora */ }

/* Comentario de una línea: empieza con | y se ignora hasta fin de línea */
"|"[^\r\n]*    { /* se ignora */ }

/* Comentario multilínea: empieza con є y termina con э */
"\u0454"       { yybegin(COMMENT); }   /* є */

<COMMENT>"\u044D" { yybegin(YYINITIAL); }  /* э */

/* Dentro del comentario multilínea ignoramos toodo */
<COMMENT>[^\r\n]+ { /* se ignora */ }
<COMMENT>\r       { /* se ignora */ }
<COMMENT>\n       { /* se ignora */ }

/* Ejemplo mínimo: palabras reservadas y símbolos */
"world"        { return tok(sym.WORLD); }
"local"        { return tok(sym.LOCAL); }
"endl"         { return tok(sym.ENDL); }

/* Delimitadores especiales del lenguaje */
"\u00BF"       { return tok(sym.LPAR, yytext()); }   /* ¿ */
"?"            { return tok(sym.RPAR, yytext()); }   /* ? */

"\u00A1"       { return tok(sym.LBRACE, yytext()); } /* ¡ */
"!"            { return tok(sym.RBRACE, yytext()); } /* ! */

/* Flecha */
"->"           { return tok(sym.ARROW, yytext()); }

/* Operadores (poner primero los de 2 caracteres) */
"//"           { return tok(sym.IDIV, yytext()); }
"++"           { return tok(sym.INC, yytext()); }
"--"           { return tok(sym.DEC, yytext()); }

"<="           { return tok(sym.LE, yytext()); }
">="           { return tok(sym.GE, yytext()); }
"=="           { return tok(sym.EQ, yytext()); }
"!="           { return tok(sym.NE, yytext()); }

"+"            { return tok(sym.PLUS, yytext()); }
"-"            { return tok(sym.MINUS, yytext()); }
"*"            { return tok(sym.TIMES, yytext()); }
"/"            { return tok(sym.DIV, yytext()); }
"%"            { return tok(sym.MOD, yytext()); }
"^"            { return tok(sym.POW, yytext()); }

"<"            { return tok(sym.LT, yytext()); }
">"            { return tok(sym.GT, yytext()); }

/* Operadores lógicos */
"@" | "and"    { return tok(sym.AND, yytext()); }
"~" | "or"     { return tok(sym.OR, yytext()); }
"\u03A3" | "not" { return tok(sym.NOT, yytext()); }

/* Números: primero float para que 12.34 no se parta */
{FLOAT}        { return tok(sym.FLOAT_LIT, Double.parseDouble(yytext())); }
{INT}          { return tok(sym.INT_LIT, Integer.parseInt(yytext())); }

/* Literales de char y string */
{CHAR}         { return tok(sym.CHAR_LIT, yytext()); }
{STRING}       { return tok(sym.STRING_LIT, yytext()); }

"true"         { return tok(sym.BOOL_LIT, true); }
"false"        { return tok(sym.BOOL_LIT, false); }

/* Palabras reservadas del lenguaje */
"decide"       { return tok(sym.DECIDE, yytext()); }
"of"           { return tok(sym.OF, yytext()); }
"else"         { return tok(sym.ELSE, yytext()); }
"end"          { return tok(sym.END, yytext()); }

"loop"         { return tok(sym.LOOP, yytext()); }
"exit"         { return tok(sym.EXIT, yytext()); }
"when"         { return tok(sym.WHEN, yytext()); }
"for"          { return tok(sym.FOR, yytext()); }

"return"       { return tok(sym.RETURN, yytext()); }
"break"        { return tok(sym.BREAK, yytext()); }

"show"         { return tok(sym.SHOW, yytext()); }
"get"          { return tok(sym.GET, yytext()); }

"gift"         { return tok(sym.GIFT, yytext()); }
"navidad"      { return tok(sym.NAVIDAD, yytext()); }
"coal"         { return tok(sym.COAL, yytext()); }
"to"           { return tok(sym.TO, yytext()); }

/* Identificadores */
{IDENT}        { return tok(sym.IDENT, yytext()); }

/* Si aparece algo que no conozco, marco ERROR solo por ese caracter */
.              { return tok(sym.ERROR, yytext()); }
