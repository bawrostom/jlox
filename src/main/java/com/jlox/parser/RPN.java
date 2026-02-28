package com.jlox.parser;

public class RPN implements ExpressionVisitor<String> {

    public String print(Expression expression) {
        return expression.getExpression().accept(this);
    }

    @Override
    public String visit(Binary expression) {
        return parenthesize(expression.operator().lexeme(),
                expression.left(), expression.left());
    }

    @Override
    public String visit(Literal expression) {
        if (expression == null) return "nil";
        return expression.value().toString();
    }

    @Override
    public String visit(Unary expression) {
        return parenthesize(expression.operator().lexeme(), expression.operand());
    }

    @Override
    public String visit(Grouping expression) {
        return parenthesize("grouping",
                expression.expression());
    }

    @Override
    public String visit(Ternary expression) {
        return parenthesize("?:",
                expression.left(), expression.middle(), expression.right());
    }

    public String parenthesize(String name, Expression... expressions) {
        StringBuilder string = new StringBuilder();
        for (Expression expression : expressions) {
            string.append(expression.accept(this)).append(" ");
        }
        string.append(name);
        return string.toString();
    }
}
