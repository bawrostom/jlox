package com.jlox.error;

import com.jlox.scanner.Token;

public class RuntimeError extends RuntimeException {
    final Token token;

    public RuntimeError(Token tokenType, String message) {
        super(message);
        this.token = tokenType;
    }
}
