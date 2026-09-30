package com.jlox.parser;

import com.jlox.error.ParseError;
import com.jlox.scanner.Token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.jlox.scanner.Token.TokenType;
import static com.jlox.scanner.Token.TokenType.*;

public class Parser {

    private final List<Token> tokens;
    private int currentPos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Statement> parse() {
        List<Statement> statements = new ArrayList<>();
        while (!end()) {
            statements.add(varDeclaration());
        }
        return statements;
    }

    public Statement varDeclaration() {
        try {
            if (match(VAR)) {
                return varStatement();
            }
            return statement();
        } catch (ParseError e) {
            synchronize();
            return null;
        }
    }

    private Statement varStatement() {
        Token name = consume(IDENTIFIER, "Expected an identifier after var key word");
        Expression initializer = null;
        if (check(EQUAL)) {
            advance();
            initializer = expression();
        }
        consume(SEMICOLON, "Expected ';' after value.");
        return new VarStmnt(name, initializer);
    }

    private Statement statement() {
        if (match(PRINT)) return printStatment();
        if (match(LEFT_BRACE)) return new BlockStmnt(block());
        if (match(IF)) return ifStatement();
        if (match(WHILE)) return whileStatement();
        if (match(FOR)) return forStatement();
        return expressionStatement();
    }

    private Statement forStatement() {
        consume(LEFT_PAREN, "Expected '(' after 'for'.");
        Statement initializer;
        if (match(SEMICOLON)) {
            initializer = null;
        } else if (match(VAR)) {
            initializer = varStatement();
        } else {
            initializer = expressionStatement();
        }
        Expression condition = null;
        if (!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "Expected ';' after value.");

        Expression increment = null;
        if (!check(RIGHT_PAREN)) {
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expected ')' after value.");

        Statement body = statement();

        if (increment != null) {
            body = new BlockStmnt(Arrays.asList(body, new ExpressionStmnt(increment)));
        }
        if (condition == null) condition = new Literal(true);
        body = new WhileStmnt(condition, body);

        if (initializer != null) body = new BlockStmnt(Arrays.asList(initializer, body));
        return body;
    }

    private Statement expressionStatement() {
        Expression expr = expression();
        consume(SEMICOLON, "Expected ';' after value.");
        return new ExpressionStmnt(expr);
    }

    private Statement printStatment() {
        Expression expr = expression();
        consume(SEMICOLON, "Expected ';' after value.");
        return new PrintStmnt(expr);
    }

    private Statement whileStatement() {
        consume(LEFT_PAREN, "Expected '(' after 'if'.");
        Expression condition = expression();
        consume(RIGHT_PAREN, "Expected ')' after expression.");
        Statement body = statement();
        return new WhileStmnt(condition, body);
    }

    private List<Statement> block() {
        List<Statement> statements = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !end()) {
            statements.add(varDeclaration());
        }
        consume(RIGHT_BRACE, "Expected '}' after value.");
        return statements;
    }

    private Statement ifStatement() {
        consume(LEFT_PAREN, "Expected '(' after 'if'.");
        Expression condition = expression();
        consume(RIGHT_PAREN, "Expected ')' after expression.");
        Statement thenStatement = statement();
        Statement branchStatement = null;
        if (match(ELSE)) {
            branchStatement = statement();
        }
        return new IfStatmnt(condition, thenStatement, branchStatement);
    }

    // expression -> equality
    private Expression expression() {
        return comma();
    }

    // comma            → assignment ( ( "," ) assignment )*
    private Expression comma() {
        Expression left = assignment();
        while (match(COMMA)) {
            left = assignment();
        }
        return left;
    }

    //    assignment     → ternary ( ("=") assignment)?
    private Expression assignment() {
        Expression expression = ternary();
        if (match(EQUAL)) {
            Token equals = previous();
            Expression value = assignment();

            if (expression instanceof Variable) {
                Token name = ((Variable) expression).name();
                return new Assign(name, value);
            }
            ParseError.error(equals, "Invalid assignment target.");
        }
        return expression;
    }

    //    ternary      -> logical_or ? expression : ternary
    //                   | logical_or
    private Expression ternary() {
        Expression left = logical_or();
        if (match(QMARK)) {
            Expression middle = expression();
            consume(COLON, "Expected token \":\"");
            return new Ternary(left, middle, ternary());
        }
        return left;
    }

    //    logical_or       → logical_end ( ( "||" ) logical_end )* ;
    private Expression logical_or() {
        if (match(OR)) {
            Token operator = advance();
            logical_end();
            ParseError.error(operator, "Operation not supported: A left hand operand is expected");
            return null;
        }

        Expression left = logical_end();
        while (match(OR)) {
            Token operator = previous();
            Expression right = logical_end();
            left = new Binary(left, operator, right);
        }
        return left;
    }

    //    logical_end       → equality ( ( "&&" ) equality )* ;
    private Expression logical_end() {
        if (match(AND)) {
            Token operator = advance();
            equality();
            ParseError.error(operator, "Operation not supported: A left hand operand is expected");
            return null;
        }

        Expression left = equality();
        while (match(AND)) {
            Token operator = previous();
            Expression right = equality();
            left = new Binary(left, operator, right);
        }
        return left;
    }

    //    equality       → comparison ( ( "!=" | "==" ) comparison )* ;
    private Expression equality() {
        if (match(BANG_EQUAL, EQUAL_EQUAL)) {
            // Sync point
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
            // Sync point
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
        if (match(PLUS)) {
            // Sync point
            Token operator = previous();
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
            // Sync point
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
    //                   | primary ;
    private Expression unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            return new Unary(operator, primary());
        }
        return primary();
    }

    //    primary        → NUMBER | STRING | "true" | "false" | "nil"
    //                  | "(" expression ")"
    //                  | IDENTIFIER;
    private Expression primary() {
        if (match(FALSE)) return new Literal(false);
        if (match(TRUE)) return new Literal(true);
        if (match(NIL)) return new Literal(null);
        if (match(STRING)) return new Literal(previous().lexeme());
        if (match(NUMBER)) return new Literal(Double.parseDouble(previous().lexeme()));
        if (match(IDENTIFIER)) return new Variable(previous());

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
        return tokens.get(currentPos).type().equals(EOF);
    }

}
