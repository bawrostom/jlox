package com.jlox.parser;

import com.jlox.scanner.Token;

public record ReturnStmt(Expression value, Token keyword) implements Statement {

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
