package com.jlox.parser;

public record PrintStmnt(Expression expression) implements Statement {

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
