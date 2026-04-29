package com.example.dbeaver.jinja.core.filters;

import java.util.List;

public final class DefaultFilter implements TemplateFilter {
    @Override
    public Object apply(Object value, List<Object> arguments) {
        if (value == null || String.valueOf(value).isEmpty()) {
            return arguments.isEmpty() ? "" : arguments.get(0);
        }
        return value;
    }
}

