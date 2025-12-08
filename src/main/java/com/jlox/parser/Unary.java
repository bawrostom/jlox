package com.jlox.parser;

import com.jlox.scanner.Token;

public record Unary(Token operator, Expression operand) implements Expression {

    @Override
    public Unary getExpression() {
        return this;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }

}
