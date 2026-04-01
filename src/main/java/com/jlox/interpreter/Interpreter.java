package com.jlox.interpreter;

import com.jlox.error.RuntimeError;
import com.jlox.parser.*;
import com.jlox.scanner.Token;

public class Interpreter implements ExpressionVisitor<Object> {
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
                return (double) left / (double) right;
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

    private boolean checkNumberOperand(Token operator, Object... operands) {
        for (Object operand : operands) if (!(operand instanceof Double)) return false;
        throw new RuntimeError(operator, "Operand must be a number");
    }

    @Override
    public Object visit(Grouping expression) {
        return expression.accept(this);
    }

    @Override
    public Object visit(Ternary expression) {
        return null;
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
}
