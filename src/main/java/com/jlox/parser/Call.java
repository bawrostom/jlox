package com.jlox.parser;

import java.util.ArrayList;

public record Call(Expression callee, ArrayList<Expression> args) implements Expression {

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
