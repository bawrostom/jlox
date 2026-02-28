package com.jlox.parser;

public interface Expression {

    public Expression getExpression();

    public <R> R accept(ExpressionVisitor<R> visitor);
}
