package com.jlox.parser;

public record ExpressionStmnt(Expression expression) implements Statement {

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
