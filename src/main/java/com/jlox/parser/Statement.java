package com.jlox.parser;

public interface Statement {

    public <R> R accept(StatementVisitor<R> visitor);

}
