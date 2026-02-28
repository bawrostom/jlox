package com.jlox.parser;

public record Grouping(Expression expression) implements Expression {

    @Override
    public Expression getExpression() {
        return this;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
