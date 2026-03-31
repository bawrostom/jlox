package com.jlox.interpreter;

import com.jlox.parser.*;

public class Interpreter implements ExpressionVisitor<Object> {
    @Override
    public Object visit(Binary expression) {
        Object left = expression.left().accept(this);
        Object right = expression.right().accept(this);

        switch (expression.operator().type()) {
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                } else if (left instanceof String && right instanceof String) {
                    return (String) left + right;
                }
                break;
            case MINUS:
                return (double) left - (double) right;
            case STAR:
                return (double) left * (double) right;
            case SLASH:
                return (double) left / (double) right;
            case GREATER:
                return (double) left > (double) right;
            case GREATER_EQUAL:
                return (double) left >= (double) right;
            case LESS:
                return (double) left < (double) right;
            case LESS_EQUAL:
                return (double) left <= (double) right;
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                expression.left().getClass();
                break;
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
        Object right = expression.operand().accept(this);
        switch (expression.operator().type()) {
            case MINUS -> {
                return -(double) right;
            }
            case BANG -> {
                return !isTruthy(right);
            }
        }
        return null;
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
