package com.example.dbeaver.jinja.core.ast;

import com.example.dbeaver.jinja.core.TemplateContext;
import com.example.dbeaver.jinja.core.parser.Expression;
import com.example.dbeaver.jinja.core.render.TemplateRenderer;

public final class VariableNode implements Node {
    private final Expression expression;

    public VariableNode(Expression expression) {
        this.expression = expression;
    }

    @Override
    public void render(TemplateContext context, TemplateRenderer renderer, StringBuilder out) {
        Object value = expression.evaluate(context, renderer);
        out.append(renderer.stringify(value));
    }
}

