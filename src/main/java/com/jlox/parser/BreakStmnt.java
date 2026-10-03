package com.jlox.parser;

import com.jlox.scanner.Token;

public record BreakStmnt(Token token) implements Statement {

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
