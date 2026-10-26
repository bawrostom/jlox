package com.jlox.parser;

public interface Expression {
    
    public <R> R accept(ExpressionVisitor<R> visitor);
}
