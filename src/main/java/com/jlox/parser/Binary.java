package com.jlox.parser;

import com.jlox.scanner.Token;

public record Binary(Expression left, Token operator, Expression right) implements Expression {

    @Override
    public Binary getExpression() {
        return this;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }

}

