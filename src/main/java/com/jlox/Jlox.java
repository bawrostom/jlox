package com.jlox;

import com.jlox.error.Error;
import com.jlox.error.ParseError;
import com.jlox.interpreter.Interpreter;
import com.jlox.parser.Parser;
import com.jlox.parser.Statement;
import com.jlox.scanner.Scanner;
import com.jlox.scanner.Token;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Jlox {

    private static final Interpreter interpreter = new Interpreter();

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    public static void runFile(String path) throws IOException {
        byte[] inputFile = Files.readAllBytes(Paths.get(path));
        run(new String(inputFile, Charset.defaultCharset()));

        // Indicate an error in the exit code.
        if (Error.getHandlerError()) System.exit(65);
        if (Error.getRuntimeHandlerError()) System.exit(70);
    }

    public static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        for (; ; ) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            Error.setHandlerError(false);
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        // For now, just print the tokens.
        for (Token token : tokens) {
            System.out.println(token);
        }
        Parser parser = new Parser(tokens);
        List<Statement> statements = null;
        try {
            statements = parser.parse();
        } catch (ParseError e) {
            e.printStackTrace();
        }
        if (Error.getHandlerError()) {
            System.out.println("Syntax error");
            return;
        } else {
//            System.out.println(new AstPrinter().print(new Binary(new Literal(new Integer(6)), new Token(Token.TokenType.PLUS, "+", null, 0), new Literal(new Integer(10)))));
//            System.out.println(new AstPrinter().print(expr));
        }

        for (Statement stmt : statements) {
            if (stmt != null) {
                interpreter.interpret(stmt);
            }
        }
    }
}