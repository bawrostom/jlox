package com.jlox.error;

import java.util.logging.Logger;

public class Error {
    private static boolean hadError;
    private static Logger logger = Logger.getLogger(Error.class.getName());

    public static void error(int line, String message) {
        report(line, "", message);
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
