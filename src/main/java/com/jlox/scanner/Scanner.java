package com.jlox.scanner;

import com.jlox.error.Error;
import com.jlox.scanner.Token.TokenType;

import static com.jlox.scanner.Token.TokenType.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Scanner {

    private final String source;
    private int line = 1;
    private int currentLexm = 0;
    private int currentPos = 0;
    private final List<Token> tokens;
    private static final HashMap<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }


    public Scanner(String source) {
        this.source = source;
        this.tokens = new ArrayList<>();
    }

    public List<Token> scanTokens() {
        while (!end()) {
            currentLexm = currentPos;
            scanToken();
        }
        addToken(EOF);
        return tokens;
    }

    public void addToken(TokenType type) {
        addToken(type, null);
    }

    public boolean end() {
        return currentPos >= source.length();
    }

    public void addToken(TokenType type, Object literal) {
        String lexm = source.substring(currentLexm, currentPos);
        tokens.add(new Token(type, lexm, literal, line));
    }

    public void scanToken() {
        char c = nextChar();
        switch (c) {
            case '+':
                addToken(PLUS);
                break;
            case '-':
                addToken(MINUS);
                break;
            case '*':
                addToken(STAR);
                break;
            case '(':
                addToken(LEFT_PAREN);
                break;
            case ')':
                addToken(RIGHT_PAREN);
                break;
            case '{':
                addToken(RIGHT_BRACE);
                break;
            case '}':
                addToken(LEFT_BRACE);
                break;
            case '.':
                addToken(DOT);
                break;
            case ';':
                addToken(SEMICOLON);
                break;
            case ',':
                addToken(COMMA);
                break;
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '/':
                checkComment();
                break;
            case '\t':
            case ' ':
                break;
            case '\n':
                line++;
                break;
            case '"':
                while (true) {
                    if (peek() == '"') {
                        nextChar();
                        break;
                    } else if (peek() == '\0') {
                        Error.error(line, "Unexpected character.");
                        return;
                    }
                    nextChar();
                }
                String stringLiteral = getSubString();
                addToken(STRING, stringLiteral);
                break;
            default:
                if (isDigit(c)) {
                    while (isDigit(peek())) {
                        nextChar();
                    }
                    if ((peek() == '.' && isDigit(peekNextChar()))) {
                        do nextChar();
                        while (isDigit(peek()));
                    }
                    Double numberLiteral = Double.parseDouble(getSubString());
                    addToken(NUMBER, numberLiteral);
                } else if (isAlpha(c)) {
                    identifier(c);
                } else {
                    Error.error(line, "Unexpected character.");
                }

        }
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void identifier(char c) {
        while (!end() && isAlphaNumeric(c)) {
            nextChar();
        }
        String lexm = source.substring(currentLexm, currentPos);
        TokenType tokenType = keywords.get(lexm) == null ? IDENTIFIER : keywords.get(lexm);
        addToken(tokenType);
    }

    private char nextChar() {
        return source.charAt(currentPos++);
    }

    private boolean match(char expected) {
        if (end()) return false;
        if (peek() != expected) return false;
        nextChar();
        return true;
    }

    private void checkComment() {
        if (end()) {
            addToken(SLASH);
            return;
        }
        char next = peek();
        switch (next) {
            case '*':
                skipCommentBlock();
                break;
            case '/':
                skipCommentLine();
                break;
            default:
                addToken(SLASH);
        }
    }

    private void skipCommentBlock() {
        int depth = 1;
        nextChar();
        char c;
        while (depth > 0 && !end()) {
            c = peek();
            if (c == '\n') line++;
            if (c == '/' && peekNextChar() == '*') {
                nextChar();
                nextChar();
                depth++;
                continue;
            }
            if (c == '*' && peekNextChar() == '/') {
                nextChar();
                nextChar();
                depth--;
                continue;
            }
            nextChar();
        }
        if (depth > 0)
            Error.error(line, "Unexpected character.");
    }

    private void skipCommentLine() {
        while (!end() && peek() != '\n') nextChar();
    }

    private char peek() {
        if (end()) return '\0';
        return source.charAt(currentPos);
    }

    private char peekNextChar() {
        if (currentPos + 1 >= source.length()) return '\0';
        return source.charAt(currentPos + 1);
    }

    private boolean isDigit(char c) {
        Pattern pattern = Pattern.compile("\\d");
        Matcher matcher = pattern.matcher(Character.toString(c));
        return matcher.find();
    }

    private String getSubString() {
        return source.substring(currentLexm, currentPos);
    }
}
