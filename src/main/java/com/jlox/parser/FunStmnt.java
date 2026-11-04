package com.jlox.parser;

import com.jlox.scanner.Token;

import java.util.List;

public record FunStmnt(Token name, List<Token> params, BlockStmnt body) implements Statement {
    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
