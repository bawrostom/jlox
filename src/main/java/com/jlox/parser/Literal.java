package com.jlox.parser;

public record Literal(Object value) implements Expression {

    @Override
    public Literal getExpression() {
        return this;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
