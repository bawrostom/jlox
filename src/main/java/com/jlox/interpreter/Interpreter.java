package com.jlox.interpreter;

import com.jlox.parser.*;

public class Interpreter implements ExpressionVisitor<Object> {
    @Override
    public Object visit(Binary expression) {
        switch (expression.operator().type()) {
            case BANG_EQUAL:
            case EQUAL_EQUAL:
                expression.left().getClass();
                break;
        }
        return null;
    }

    @Override
    public Object visit(Literal expression) {
        return expression.value();
    }

    @Override
    public Object visit(Unary expression) {
        Object right = expression.operand();
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
        return null;
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
