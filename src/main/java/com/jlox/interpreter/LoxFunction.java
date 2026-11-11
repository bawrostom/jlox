package com.jlox.interpreter;

import com.jlox.parser.FunStmnt;

import java.util.List;

public record LoxFunction(FunStmnt declaration, Environment closure) implements LoxCallable {
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment env = new Environment(closure);
        for (int i = 0; i < declaration.params().size(); i++) {
            env.define(declaration.params().get(i).lexeme(),
                    arguments.get(i));
        }
        try {
            interpreter.executeBlock(declaration.body().statements(), env);
        } catch (Return r) {
            return r.value;
        }
        return null;
    }

    @Override
    public int arity() {
        return declaration.params().size();
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name().lexeme() + ">";
    }
}
