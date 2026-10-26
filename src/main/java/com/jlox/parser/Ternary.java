package com.jlox.parser;

public record Ternary(Expression left, Expression middle, Expression right) implements Expression {

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
