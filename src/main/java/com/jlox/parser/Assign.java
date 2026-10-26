package com.jlox.parser;

import com.jlox.scanner.Token;

public record Assign(Token name, Expression value) implements Expression {

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
