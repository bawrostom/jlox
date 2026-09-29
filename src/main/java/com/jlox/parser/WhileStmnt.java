package com.jlox.parser;

public record WhileStmnt(Expression condition, Statement body) implements Statement {
    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
