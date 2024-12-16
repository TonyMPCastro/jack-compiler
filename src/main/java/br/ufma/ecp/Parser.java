package br.ufma.ecp;

import static br.ufma.ecp.token.TokenType.CONSTRUCTOR;
import static br.ufma.ecp.token.TokenType.DO;
import static br.ufma.ecp.token.TokenType.IDENT;
import static br.ufma.ecp.token.TokenType.IF;
import static br.ufma.ecp.token.TokenType.LET;
import static br.ufma.ecp.token.TokenType.METHOD;
import static br.ufma.ecp.token.TokenType.RETURN;
import static br.ufma.ecp.token.TokenType.WHILE;
import static br.ufma.ecp.token.TokenType.STRING;
import static br.ufma.ecp.token.TokenType.INT;
import static br.ufma.ecp.token.TokenType.FALSE;
import static br.ufma.ecp.token.TokenType.THIS;
import static br.ufma.ecp.token.TokenType.NULL;
import static br.ufma.ecp.token.TokenType.TRUE;
import static br.ufma.ecp.token.TokenType.LBRACKET;
import static br.ufma.ecp.token.TokenType.LPAREN;
import static br.ufma.ecp.token.TokenType.MINUS;
import static br.ufma.ecp.token.TokenType.NOT;
import static br.ufma.ecp.token.TokenType.DOT;
import static br.ufma.ecp.token.TokenType.RBRACKET;
import static br.ufma.ecp.token.TokenType.RPAREN;

import br.ufma.ecp.token.Token;
import br.ufma.ecp.token.TokenType;

public class Parser {

    private static class ParseError extends RuntimeException {
    }

    private Scanner scan;
    private Token currentToken;
    private Token peekToken;
    private StringBuilder xmlOutput = new StringBuilder();

    private String className; // nome dae uma class

    public Parser(byte[] input) {
        scan = new Scanner(input);
        nextToken();
    }

    private void nextToken() {
        currentToken = peekToken;
        peekToken = scan.nextToken();
    }

    public void parse() {
        parseClass();
    }

    public void parseClass() {
        printNonTerminal("class");
        expectPeek(TokenType.CLASS);
        expectPeek(TokenType.IDENT);
        className = currentToken.value();
        expectPeek(TokenType.LBRACE);

        while (peekTokenIs(TokenType.STATIC) || peekTokenIs(TokenType.FIELD)) {
            parseClassVarDec();
        }

        while (peekTokenIs(TokenType.FUNCTION) || peekTokenIs(TokenType.CONSTRUCTOR) || peekTokenIs(TokenType.METHOD)) {
            parseSubroutineDec();
        }

        expectPeek(TokenType.RBRACE);

        printNonTerminal("/class");
    }

    // classVarDec → ( 'static' | 'field' ) type varName ( ',' varName)* ';'
    public void parseClassVarDec() {
        printNonTerminal("classVarDec");
        expectPeek(TokenType.FIELD, TokenType.STATIC);

        // 'int' | 'char' | 'boolean' | className
        expectPeek(TokenType.INT, TokenType.CHAR, TokenType.BOOLEAN, TokenType.IDENT);
        expectPeek(TokenType.IDENT);

        while (peekTokenIs(TokenType.COMMA)) {
            expectPeek(TokenType.COMMA);
            expectPeek(TokenType.IDENT);
        }

        expectPeek(TokenType.SEMICOLON);
        printNonTerminal("/classVarDec");
    }

    public void parseParameterList() {
        printNonTerminal("parameterList");

        if (!peekTokenIs(TokenType.RPAREN)) // verifica se tem pelo menos uma expressao
        {
            expectPeek(TokenType.INT, TokenType.CHAR, TokenType.BOOLEAN, TokenType.IDENT);
            expectPeek(TokenType.IDENT);

            while (peekTokenIs(TokenType.COMMA)) {
                expectPeek(TokenType.COMMA);
                expectPeek(TokenType.INT, TokenType.CHAR, TokenType.BOOLEAN, TokenType.IDENT);
                expectPeek(TokenType.IDENT);
            }

        }

        printNonTerminal("/parameterList");
    }

    public void parseSubroutineBody(String functionName, TokenType subroutineType) {
        printNonTerminal("subroutineBody");
        expectPeek(TokenType.LBRACE);
        while (peekTokenIs(TokenType.VAR)) {
            parseVarDec();
        }
        parseStatements();
        expectPeek(TokenType.RBRACE);
        printNonTerminal("/subroutineBody");
    }

    public void parseVarDec() {
        printNonTerminal("varDec");
        expectPeek(TokenType.VAR);

        // 'int' | 'char' | 'boolean' | className
        expectPeek(TokenType.INT, TokenType.CHAR, TokenType.BOOLEAN, TokenType.IDENT);

        expectPeek(TokenType.IDENT);

        while (peekTokenIs(TokenType.COMMA)) {
            expectPeek(TokenType.COMMA);
            expectPeek(TokenType.IDENT);
        }

        expectPeek(TokenType.SEMICOLON);
        printNonTerminal("/varDec");

    }

    public void parseStatements() {
        printNonTerminal("statements");
        while (peekToken.type == TokenType.WHILE ||
                peekToken.type == TokenType.IF ||
                peekToken.type == TokenType.LET ||
                peekToken.type == TokenType.DO ||
                peekToken.type == TokenType.RETURN) {
            parseStatement();
        }

        printNonTerminal("/statements");
    }

    public void parseTerm() {
        printNonTerminal("term");
        switch (peekToken.type) {
            case INT:
                expectPeek(INT);
                break;
            case NUMBER:
                expectPeek(TokenType.NUMBER);
                break;
            case STRING:
                expectPeek(STRING);
                break;
            case FALSE:
            case NULL:
            case TRUE:
                expectPeek(FALSE, NULL, TRUE);
                break;
            case THIS:
                expectPeek(THIS);
                break;
            case IDENT:
                expectPeek(IDENT);
                if (peekTokenIs(LPAREN) || peekTokenIs(DOT)) {
                    parseSubroutineCall();
                } else { // variavel comum ou array
                    if (peekTokenIs(LBRACKET)) { // array
                        expectPeek(LBRACKET);
                        parseExpression();
                        expectPeek(RBRACKET);

                    }
                }
                break;
            case LPAREN:
                expectPeek(LPAREN);
                parseExpression();
                expectPeek(RPAREN);
                break;
            case MINUS:
            case NOT:
                expectPeek(MINUS, NOT);
                parseTerm();
                break;
            default:
                System.err.println(peekToken.type);
                throw error(peekToken, "term expected");
        }
        printNonTerminal("/term");
    }

    public void parseSubroutineDec() {
        printNonTerminal("subroutineDec");

        expectPeek(TokenType.CONSTRUCTOR, TokenType.FUNCTION, TokenType.METHOD);

        var subroutineType = currentToken.type;

        // 'int' | 'char' | 'boolean' | className
        expectPeek(TokenType.VOID, TokenType.INT, TokenType.CHAR, TokenType.BOOLEAN, TokenType.IDENT);
        expectPeek(TokenType.IDENT);

        var functionName = className + "." + currentToken.value();

        expectPeek(TokenType.LPAREN);
        parseParameterList();
        expectPeek(TokenType.RPAREN);
        parseSubroutineBody(functionName, subroutineType);

        printNonTerminal("/subroutineDec");
    }

    // expression -> term (op term)*
    public void parseExpression() {
        printNonTerminal("expression");
        parseTerm();
        while (isOperator(peekToken.lexeme)) {
            expectPeek(peekToken.type);
            parseTerm();
        }
        printNonTerminal("/expression");
    }

    // letStatement -> 'let' identifier( '[' expression ']' )? '=' expression ';'
    public void parseLet() {

        var isArray = false;

        printNonTerminal("letStatement");
        expectPeek(TokenType.LET);
        expectPeek(TokenType.IDENT);

        if (peekTokenIs(TokenType.LBRACKET)) { // array
            expectPeek(TokenType.LBRACKET);
            parseExpression();
            expectPeek(TokenType.RBRACKET);
            isArray = true;
        }

        expectPeek(TokenType.EQ);
        parseExpression();

        expectPeek(TokenType.SEMICOLON);
        printNonTerminal("/letStatement");
    }

    // subroutineCall -> subroutineName '(' expressionList ')' |
    // (className|varName)'.'subroutineName '('expressionList ')
    public void parseSubroutineCall() {
        var nArgs = 0;

        var ident = currentToken.value();
        var functionName = ident + ".";

        if (peekTokenIs(LPAREN)) { // método da propria classe

            expectPeek(LPAREN);
            nArgs = parseExpressionList() + 1;
            expectPeek(RPAREN);
            functionName = className + "." + ident;

        } else {
            // pode ser um metodo de um outro objeto ou uma função
            expectPeek(DOT);
            expectPeek(IDENT); // nome da função
            expectPeek(LPAREN);
            nArgs += parseExpressionList();
            expectPeek(RPAREN);
        }
    }

    public int parseExpressionList() {
        printNonTerminal("expressionList");

        var nArgs = 0;

        if (!peekTokenIs(TokenType.RPAREN)) // verifica se tem pelo menos uma expressao
        {
            parseExpression();
            nArgs = 1;
        }

        // procurando as demais
        while (peekTokenIs(TokenType.COMMA)) {
            expectPeek(TokenType.COMMA);
            parseExpression();
            nArgs++;
        }

        printNonTerminal("/expressionList");
        return nArgs;
    }

    public void parseStatement() {
        switch (peekToken.type) {
            case LET:
                parseLet();
                break;
            case WHILE:
                parseWhile();
                break;
            case IF:
                parseIf();
                break;
            case RETURN:
                parseReturn();
                break;
            case DO:
                parseDo();
                break;
            default:
                throw error(peekToken, "Expected a statement");
        }
    }

    // 'while' '(' expression ')' '{' statements '}'
    public void parseWhile() {
        printNonTerminal("whileStatement");

        expectPeek(TokenType.WHILE);
        expectPeek(TokenType.LPAREN);
        parseExpression();

        expectPeek(TokenType.RPAREN);
        expectPeek(TokenType.LBRACE);
        parseStatements();

        expectPeek(TokenType.RBRACE);
        printNonTerminal("/whileStatement");
    }

    public void parseIf() {
        printNonTerminal("ifStatement");

        expectPeek(TokenType.IF);
        expectPeek(TokenType.LPAREN);

        parseExpression();
        expectPeek(TokenType.RPAREN);
        expectPeek(TokenType.LBRACE);

        parseStatements();
        expectPeek(TokenType.RBRACE);

        if (peekTokenIs(TokenType.ELSE)) {
            expectPeek(TokenType.ELSE);
            expectPeek(TokenType.LBRACE);

            parseStatements();
            expectPeek(TokenType.RBRACE);
        }

        printNonTerminal("/ifStatement");
    }

    // ReturnStatement -> 'return' expression? ';'
    public void parseReturn() {
        printNonTerminal("returnStatement");
        expectPeek(TokenType.RETURN);
        if (!peekTokenIs(TokenType.SEMICOLON)) {
            parseExpression();
        }
        expectPeek(TokenType.SEMICOLON);
        printNonTerminal("/returnStatement");
    }

    // 'do' subroutineCall ';'
    public void parseDo() {
        printNonTerminal("doStatement");
        expectPeek(TokenType.DO);
        expectPeek(TokenType.IDENT);
        parseSubroutineCall();
        expectPeek(TokenType.SEMICOLON);
        printNonTerminal("/doStatement");
    }

    // funções auxiliares
    public String XMLOutput() {
        return xmlOutput.toString();
    }

    private void printNonTerminal(String nterminal) {
        xmlOutput.append(String.format("<%s>\r\n", nterminal));
    }

    static public boolean isOperator(String op) {
        return op != "" && "+-*/<>=~&|".contains(op);
    }

    boolean peekTokenIs(TokenType type) {
        return peekToken.type == type;
    }

    boolean currentTokenIs(TokenType type) {
        return currentToken.type == type;
    }

    private void expectPeek(TokenType... types) {
        for (TokenType type : types) {
            if (peekToken.type == type) {
                expectPeek(type);
                return;
            }
        }

        throw error(peekToken, "Expected a statement");

    }

    private void expectPeek(TokenType type) {
        if (peekToken.type == type) {
            nextToken();
            xmlOutput.append(String.format("%s\r\n", currentToken.toString()));
        } else {
            throw error(peekToken, "Expected " + type.name());
        }
    }

    private static void report(int line, String where,
            String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message);
    }

    private ParseError error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
        return new ParseError();
    }

}