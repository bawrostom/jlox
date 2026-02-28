package com.jlox.parser;

public interface ExpressionVisitor<R> {

    public R visit(Binary expression);

    public R visit(Literal expression);

    public R visit(Unary expression);

    public R visit(Grouping expression);

    public R visit(Ternary expression);

}
