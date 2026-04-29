package com.example.dbeaver.jinja.core.parser;

import com.example.dbeaver.jinja.core.TemplateContext;
import com.example.dbeaver.jinja.core.render.TemplateRenderer;

import java.util.Objects;

public final class Condition {
    private final boolean negated;
    private final Expression left;
    private final Expression right;

    public Condition(boolean negated, Expression left, Expression right) {
        this.negated = negated;
        this.left = left;
        this.right = right;
    }

    public boolean evaluate(TemplateContext context, TemplateRenderer renderer) {
        boolean value;
        if (right == null) {
            value = renderer.isTruthy(left.evaluate(context, renderer));
        } else {
            value = Objects.equals(left.evaluate(context, renderer), right.evaluate(context, renderer));
        }
        return negated ? !value : value;
    }
}

