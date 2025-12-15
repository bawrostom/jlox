package com.jlox.parser;

public class AstPrinter implements ExpressionVisitor<String> {

    public String print(Expression expression) {
        return expression.accept(this);
    }

    @Override
    public String visit(Binary expression) {
        return parenthesize(expression.operator().lexeme(),
                expression.left(), expression.left());
    }

    @Override
    public String visit(Literal expression) {
        if (expression.value() == null) return "nil";
        return expression.value().toString();
    }

    @Override
    public String visit(Unary expression) {
        return parenthesize(expression.operator().lexeme(),
                expression.operand());
    }

    public String visit(Grouping expression) {
        return parenthesize("group", expression.expression());
    }


    public String parenthesize(String name, Expression... expressions) {
        StringBuilder string = new StringBuilder();
        string.append("(").append(name);
        for (Expression expression : expressions) {
            string.append(" ").append(expression.accept(this));
        }
        string.append(")");
        return string.toString();
    }
}
