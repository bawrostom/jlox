package com.jlox.parser;

public interface StatementVisitor<R> {

    public R visit(ExpressionStmnt statement);

    public R visit(PrintStmnt statement);

    public R visit(VarStmnt statement);

    public R visit(BlockStmnt statement);
}

