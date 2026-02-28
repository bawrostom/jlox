package com.jlox.error;

import com.jlox.scanner.Token;

import java.util.logging.Logger;

import static com.jlox.scanner.Token.TokenType.EOF;

public class Error {
    private static boolean hadError;
    private static Logger logger = Logger.getLogger(Error.class.getName());

    public static void error(int line, String message) {
        report(line, "", message);
    }

    public static void error(Token token, String message) {
        if (token.type() == EOF) {
            report(token.line(), " at end", message);
        } else {
            report(token.line(), " at '" + token.lexeme() + "'", message);
        }
    }

    private static void report(int line, String where,
                               String message) {
        System.out.println(
                "[line " + line + "] Error" + where + ": " + message);
        setHandlerError(true);
    }

    public static boolean getHandlerError() {
        return hadError;
    }

    public static void setHandlerError(boolean error) {
        hadError = error;
    }
}
