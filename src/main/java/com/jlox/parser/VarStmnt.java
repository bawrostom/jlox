package com.jlox.parser;

import com.jlox.scanner.Token;

public record VarStmnt(Token name, Expression initializer) implements Statement {
    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
