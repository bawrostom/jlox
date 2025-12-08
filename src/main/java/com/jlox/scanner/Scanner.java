package com.jlox.scanner;

import com.jlox.error.Error;
import com.jlox.scanner.Token.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Scanner {

    private final String source;
    private int line = 1;
    private int currentLexm = 0;
    private int currentPos = 0;
    private final List<Token> tokens;
//    private char[] sourceChar;

    public Scanner(String source) {
        this.source = source;
        this.tokens = new ArrayList<>();
//        sourceChar = source.toCharArray();
    }

    public List<Token> scanTokens() {
        while (!end()) {
            currentLexm = currentPos;
            scanToken();
        }
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
                addToken(TokenType.PLUS);
                break;
            case '-':
                addToken(TokenType.MINUS);
                break;
            case '*':
                addToken(TokenType.STAR);
                break;
            case '(':
                addToken(TokenType.LEFT_PAREN);
                break;
            case ')':
                addToken(TokenType.RIGHT_PAREN);
                break;
            case '{':
                addToken(TokenType.RIGHT_BRACE);
                break;
            case '}':
                addToken(TokenType.LEFT_BRACE);
                break;
            case '.':
                addToken(TokenType.DOT);
                break;
            case ';':
                addToken(TokenType.SEMICOLON);
                break;
            case ',':
                addToken(TokenType.COMMA);
                break;
            case '!':
                addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                break;
            case '>':
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;
            case '<':
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            case '=':
                addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
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
                addToken(TokenType.STRING, stringLiteral);
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
                    addToken(TokenType.NUMBER, numberLiteral);
                } else {
                    Error.error(line, "Unexpected character.");
                }

        }
//        List<Token> tokens = new ArrayLis
//        t<>();
//        TokenType token = null;
//        int line = 0;
//        String lexem = "";
//        StringBuilder lexemStringBuilder = new StringBuilder();
//
//        for (char c : source.toCharArray()) {
//            line += (c == '\n') ? 1 : 0;
//            if (c == ' ') {
//                lexem = lexemStringBuilder.toString();
//                tokens.add(new Token(null, lexem, null, line));null
//                lexemStringBuilder.delete(0, lexemStringBuilder.length());
//                break;
//            }
//            lexemStringBuilder.append(c);
//        }
//        return tokens;
//    }
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
            addToken(TokenType.SLASH);
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
                addToken(TokenType.SLASH);
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
