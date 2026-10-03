package com.jlox.interpreter;

import com.jlox.error.Error;
import com.jlox.error.RuntimeError;
import com.jlox.error.ZeroDivisionError;
import com.jlox.parser.*;
import com.jlox.scanner.Token;

import java.util.List;

import static com.jlox.scanner.Token.TokenType.OR;

public class Interpreter implements ExpressionVisitor<Object>, StatementVisitor<Object> {

    private Environment env = new Environment();

    private boolean whileFlag;
    private boolean breakFlag;


    public Object interpret(Statement statement) {
        try {
            Object value = statement.accept(this);
//           System.out.println(stringify(value));
            return value;
        } catch (RuntimeError e) {
            Error.runtimeError(e);
        }
        return null;
    }

    public String stringify(Object value) {
        if (value == null) return "nil";
        if (value instanceof Double) {
            String text = value.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return value.toString();
    }

    @Override
    public Object visit(Binary expression) {
        Object left = expression.left().accept(this);
        Object right = expression.right().accept(this);
        Token operator = expression.operator();

        switch (expression.operator().type()) {
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                } else if (left instanceof String && right instanceof String) {
                    return (String) left + right;
                } else if (left instanceof String || right instanceof String) {
                    return String.valueOf(left) + String.valueOf(right);
                }
                throw new RuntimeError(operator, "Operands must be two numbers or two strings.");
            case MINUS:
                checkNumberOperand(operator, left, right);
                return (double) left - (double) right;
            case STAR:
                checkNumberOperand(operator, left, right);
                return (double) left * (double) right;
            case SLASH:
                checkNumberOperand(operator, left, right);
                double result = ((double) left / (double) right);
                if (Double.isNaN(result) || Double.isInfinite(result)) {
                    throw new ZeroDivisionError(expression.operator());
                }
                return result;
            case GREATER:
                checkNumberOperand(operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperand(operator, left, right);
                return (double) left >= (double) right;
            case LESS:
                checkNumberOperand(operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperand(operator, left, right);
                return (double) left <= (double) right;
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
        }
        return null;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b);
    }

    @Override
    public Object visit(Literal expression) {
        return expression.value();
    }

    @Override
    public Object visit(Unary expression) {
        Object right = null;
        right = expression.operand().accept(this);
        switch (expression.operator().type()) {
            case MINUS:
                checkNumberOperand(expression.operator(), right);
                return -(double) right;
            case BANG:
                return !isTruthy(right);
        }
        return null;
    }

    private void checkNumberOperand(Token operator, Object... operands) {
        for (Object operand : operands)
            if (!(operand instanceof Double)) throw new RuntimeError(operator, "Operand must be a number");
    }

    @Override
    public Object visit(Grouping expression) {
        return expression.accept(this);
    }

    @Override
    public Object visit(Ternary expression) {
        Object left = expression.left().accept(this);
        Object middle = expression.middle().accept(this);
        Object right = expression.right().accept(this);

        if (isTruthy(left)) {
            return middle;
        }
        return right;
    }

    private boolean isTruthy(Object right) {
        if (right == null) {
            return false;
        }
        if (right instanceof Boolean) {
            return (boolean) right;
        }
        return true;
    }

    @Override
    public Object visit(ExpressionStmnt statement) {
        return statement.expression().accept(this);
    }

    @Override
    public Void visit(PrintStmnt statement) {
        Object expr = statement.expression().accept(this);
        if (expr instanceof String || expr instanceof Interpreter || expr instanceof Double) {
            System.out.print(expr);
        }
//        throw new RuntimeError(null, "Operands must be two numbers or two strings.");
        return null;
    }

    @Override
    public Void visit(VarStmnt statement) {
        Object val = null;
        if (statement.initializer() != null) {
            val = statement.initializer().accept(this);
        }
        env.define(statement.name().lexeme(), val);
        return null;
    }

    @Override
    public Void visit(BlockStmnt blockStatement) {
        executeBlock(blockStatement.statements(), new Environment(env));
        return null;
    }

    @Override
    public Object visit(IfStatmnt statement) {
        Object condition = statement.condition().accept(this);
        if (isTruthy(condition)) {
            statement.thenBranch().accept(this);
        } else if (statement.elseBranch() != null) {
            statement.elseBranch().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(WhileStmnt whileStmnt) {
        whileFlag = true;
        Expression condition = whileStmnt.condition();
        while (isTruthy(condition.accept(this))) {
            whileStmnt.body().accept(this);
            if (breakFlag) {
                breakFlag = false;
                break;
            }
        }
        whileFlag = false;
        return null;
    }

    @Override
    public Object visit(BreakStmnt breakStmnt) {
        if (whileFlag) {
            breakFlag = true;
            return null;
        }
        throw new RuntimeError(breakStmnt.token(), "Unexpected 'break' statement");
    }

    private void executeBlock(List<Statement> statements, Environment environment) {
        Environment previous = this.env;
        try {
            this.env = environment;
            for (Statement statement : statements) {
                statement.accept(this);
            }
        } finally {
            this.env = previous;
        }
    }

    @Override
    public Object visit(Variable expression) {
        return env.get(expression.name());
    }

    @Override
    public Object visit(Assign expression) {
        Object value = expression.value().accept(this);
        env.assign(expression.name(), value);
        return value;
    }

    @Override
    public Object visit(Logical expression) {
        Object left = expression.left().accept(this);
        Token operator = expression.operator();

        if (operator.type() == OR) {
            if (isTruthy(expression.left())) return left;
        } else if (!isTruthy(expression.left())) return left;

        return expression.right().accept(this);
    }
}
