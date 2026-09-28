package com.jlox.parser;

public record IfStatmnt(Expression condition, Statement thenBranch, Statement elseBranch) implements Statement {
    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visit(this);
    }
}