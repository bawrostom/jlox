package com.jlox.parser;

import java.util.List;

public record BlockStmnt(List<Statement> statements) implements Statement {
    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
