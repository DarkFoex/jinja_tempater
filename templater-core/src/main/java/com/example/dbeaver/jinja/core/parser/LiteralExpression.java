package com.example.dbeaver.jinja.core.parser;

import com.example.dbeaver.jinja.core.TemplateContext;
import com.example.dbeaver.jinja.core.render.TemplateRenderer;

public final class LiteralExpression implements Expression {
    private final Object value;

    public LiteralExpression(Object value) {
        this.value = value;
    }

    @Override
    public Object evaluate(TemplateContext context, TemplateRenderer renderer) {
        return value;
    }

    @Override
    public String asDebugString() {
        return String.valueOf(value);
    }
}

