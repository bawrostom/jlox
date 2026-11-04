package com.jlox.parser;

import com.jlox.scanner.Token;

import java.util.ArrayList;

public record Call(Expression callee, Token paren, ArrayList<Expression> args) implements Expression {

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
