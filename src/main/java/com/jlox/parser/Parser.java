package com.jlox.parser;

import com.jlox.error.ParseError;
import com.jlox.scanner.Token;

import java.util.List;

import static com.jlox.scanner.Token.TokenType;
import static com.jlox.scanner.Token.TokenType.*;

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

    // expression -> equality
    private Expression expression() {
        return equality();
    }

    //    equality       → comparison ( ( "!=" | "==" ) comparison )* ;
    private Expression equality() {
        if (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = advance();
            comparison();
            ParseError.error(operator, "Operation not supported: A left hand operand is expected");
            return null;
        }

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
        if (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = advance();
            term();
            ParseError.error(operator, "Operation not supported: A left hand operand is expected");
            return null;
        }

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
        if (match(MINUS, PLUS)) {
            Token operator = advance();
            factor();
            ParseError.error(operator, "Operation not supported: A left hand operand is expected");
            return null;
        }

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
        if (match(SLASH, STAR)) {
            Token operator = advance();
            unary();
            ParseError.error(operator, "Operation not supported: A left hand operand is expected");
            return null;
        }

        Expression left = unary();
        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expression right = unary();
            left = new Binary(left, operator, right);
        }
        return left;
    }

    //    unary          → ( "!" | "-" ) unary
    //                   | ternary ;
    private Expression unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            return new Unary(operator, ternary());
        }
        return ternary();
    }

    //    ternary      -> comma ? expression : ternary
    //                   | comma
    private Expression ternary() {
        Expression left = comma();
        if (match(QMARK)) {
            Expression middle = expression();
            consume(COLON, "Expected token \":\"");
            return new Ternary(left, middle, comma());
        }
        return left;
    }


    // comma            → primary ( ( "," ) primary )*
    private Expression comma() {
        Expression left = primary();
        while (match(COMMA)) {
            Token operator = previous();
            Expression right = primary();
            left = new Binary(left, operator, right);
        }
        return left;
    }

    //    primary        → NUMBER | STRING | "true" | "false" | "nil"
    //                  | "(" expression ")" ;
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
        throw ParseError.error(peek(), "Expected expression");
    }

    private Token consume(TokenType tokenType, String errorMessage) {
        if (check(tokenType)) {
            return advance();
        }
        throw ParseError.error(peek(), errorMessage);
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
