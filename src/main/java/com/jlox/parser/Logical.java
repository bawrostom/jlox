package com.jlox.parser;

import com.jlox.scanner.Token;

public record Logical(Expression left, Token operator, Expression right) implements Expression {

    @Override
    public Expression getExpression() {
        return null;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
