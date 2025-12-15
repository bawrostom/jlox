package com.jlox.parser;

import com.jlox.scanner.Token;

import static com.jlox.scanner.Token.TokenType;
import static com.jlox.scanner.Token.TokenType.*;

import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private int currentPos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Expression expression() {
        return equality();
    }

    //    equality       → comparison ( ( "!=" | "==" ) comparison )* ;
    private Expression equality() {
        Expression left = comparison();

        while (match(EQUAL_EQUAL, BANG_EQUAL)) {
            Token operator = previous();
            Expression right = comparison();
            left = new Binary(left, operator, right);
        }
        return left;
    }

    //    comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
    private Expression comparison() {
        Expression left = term();
        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expression right = term();
            left = new Binary(left, operator, right);
        }
        return left;
    }

    //    term           → factor ( ( "-" | "+" ) factor )* ;
    private Expression term() {
        Expression left = factor();

        while (match(PLUS, MINUS)) {
            Token operator = previous();
            Expression right = factor();
            left = new Binary(left, operator, right);
        }
        return left;
    }

    //    factor         → unary ( ( "/" | "*" ) unary )* ;
    private Expression factor() {
        Expression left = Unary();

        while (match(SLASH, DOT)) {
            Token operator = previous();
            Expression right = Unary();
            left = new Binary(left, operator, right);
        }
        return left;
    }

    //    unary          → ( "!" | "-" ) unary
    //                   | primary ;
    private Expression Unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            return new Unary(operator, primary());
        }
        return primary();
    }

    private Expression primary() {
        TokenType tokenType = peek();
        switch (tokenType) {
            case NUMBER:
                new Literal(advance().literal());
            default:
                return null;
        }
    }

    private boolean match(TokenType... tokenTypes) {
        for (TokenType tokenType : tokenTypes) {
            if (check(tokenType)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token advance() {
        if (!end()) currentPos++;
        return previous();
    }

    private boolean check(TokenType tokenType) {
        if (end()) return false;
        return peek() == tokenType;
    }

    private TokenType peek() {
        return tokens.get(currentPos).type();
    }

    private Token previous() {
        return tokens.get(currentPos - 1);
    }

    private boolean end() {
        return (currentPos >= tokens.size());
    }

}
