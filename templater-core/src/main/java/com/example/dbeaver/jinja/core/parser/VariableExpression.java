package com.example.dbeaver.jinja.core.parser;

import com.example.dbeaver.jinja.core.TemplateContext;
import com.example.dbeaver.jinja.core.render.TemplateRenderer;

import java.util.ArrayList;
import java.util.List;

public final class VariableExpression implements Expression {
    private final String path;
    private final List<FilterInvocation> filters;

    public VariableExpression(String path, List<FilterInvocation> filters) {
        this.path = path;
        this.filters = filters == null ? List.of() : List.copyOf(filters);
    }

    @Override
    public Object evaluate(TemplateContext context, TemplateRenderer renderer) {
        Object value = renderer.resolvePath(context, path);
        for (FilterInvocation filter : filters) {
            List<Object> args = new ArrayList<>();
            for (Expression argument : filter.arguments()) {
                args.add(argument.evaluate(context, renderer));
            }
            value = renderer.applyFilter(filter.name(), value, args);
        }
        return value;
    }

    @Override
    public String asDebugString() {
        return path;
    }

    public record FilterInvocation(String name, List<Expression> arguments) {
    }
}

