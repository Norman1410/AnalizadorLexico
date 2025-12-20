import java_cup.runtime.Symbol;

%%

%public
%class LexicoScanner
%unicode
%cup
%line
%column

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

%%

/* =========================
   REGLAS
   ========================= */

/* Ignorar espacios y saltos */
{WHITE}        { /* se ignora */ }

/* Ejemplo mínimo: palabras reservadas y símbolos */
"world"        { return tok(sym.WORLD); }
"local"        { return tok(sym.LOCAL); }
"endl"         { return tok(sym.ENDL); }

"+"            { return tok(sym.PLUS); }
"-"            { return tok(sym.MINUS); }
"*"            { return tok(sym.TIMES); }
"%"            { return tok(sym.MOD); }
"^"            { return tok(sym.POW); }

/* Identificadores */
{IDENT}        { return tok(sym.IDENT); }

/* Si aparece algo que no conozco */
.              { return tok(sym.ERROR); }
