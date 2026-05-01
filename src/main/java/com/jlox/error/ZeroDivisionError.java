package com.jlox.error;

import com.jlox.scanner.Token;

public class ZeroDivisionError extends RuntimeError {

    public ZeroDivisionError(Token tokenType) {
        super(tokenType, "Division by zero is not allowed");
    }

}
