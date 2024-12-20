package br.ufma.ecp.token;

import java.util.List;

public enum TokenType {

    // Literals.
    NUMBER,
    STRING,

    IDENT,

    // keywords
    WHILE, CLASS, CONSTRUCTOR, FUNCTION,
    METHOD, FIELD, STATIC, VAR, INT,
    CHAR, BOOLEAN, VOID, TRUE, FALSE,
    NULL, THIS, LET, DO, IF, ELSE, RETURN,

    // symbols
    LPAREN, RPAREN,
    LBRACE, RBRACE,
    LBRACKET, RBRACKET,

    COMMA, SEMICOLON, DOT,

    PLUS, MINUS, ASTERISK, SLASH,

    AND, OR, NOT,

    LT, GT, EQ,

    EOF,

    ILLEGAL;

    private TokenType() {
    }

    private TokenType(String value) {
        this.value = value;
    }

    public String value;

    static public boolean isSymbol(String c) {
        String symbols = "{}()[].,;+-*/&|<>=~";
        return symbols.indexOf(c) > -1;
    }

    static public boolean isKeyword(TokenType type) {
        List<TokenType> keywords = List.of(
                WHILE, CLASS, CONSTRUCTOR, FUNCTION,
                METHOD, FIELD, STATIC, VAR, INT,
                CHAR, BOOLEAN, VOID, TRUE, FALSE,
                NULL, THIS, LET, DO, IF, ELSE, RETURN);
        return keywords.contains(type);
    }

}
