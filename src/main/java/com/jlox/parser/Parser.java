package com.jlox.parser;

import com.jlox.error.Error;
import com.jlox.scanner.Token;
import com.jlox.error.ParseError;

import static com.jlox.scanner.Token.TokenType;
import static com.jlox.scanner.Token.TokenType.*;

import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private int currentPos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Expression parse() {
        try {
            return expression();
        } catch (ParseError e) {
            return null;
        }
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

        while (match(SLASH, STAR)) {
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

    //    primary        → NUMBER | STRING | "true" | "false" | "nil"
    //            | "(" expression ")" ;
    private Expression primary() {
        if (match(FALSE)) {
            return new Literal(false);
        }
        if (match(TRUE)) {
            return new Literal(true);
        }
        if (match(NIL)) {
            return new Literal(null);
        }
        if (match(STRING, NUMBER)) {
            return new Literal(previous().lexeme());
        }

        if (match(LEFT_PAREN)) {
            Expression expr = expression();
            consume(RIGHT_PAREN, "Expected token \")\"");
            return new Grouping(expr);
        }
        throw error(peek(), "Expected expression");
    }

    private Token consume(TokenType tokenType, String errorMessage) {
        if (check(tokenType)) {
            return advance();
        }
        throw error(peek(), errorMessage);
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
        return peek().type() == tokenType;
    }

    private Token peek() {
        return tokens.get(currentPos);
    }

    private Token previous() {
        return tokens.get(currentPos - 1);
    }

    private ParseError error(Token token, String message) {
        Error.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!end()) {
            if (previous().type() == SEMICOLON) return;

            switch (peek().type()) {
                case CLASS:
                case FOR:
                case IF:
                case RETURN:
                case VAR:
                case WHILE:
                case FUN:
                case PRINT:
                    return;
            }
            advance();
        }
    }

    private boolean end() {
        return (currentPos >= tokens.size());
    }

}
