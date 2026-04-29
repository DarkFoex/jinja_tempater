package com.example.dbeaver.jinja.core.filters;

import java.util.List;

public final class TrimFilter implements TemplateFilter {
    @Override
    public Object apply(Object value, List<Object> arguments) {
        return value == null ? null : String.valueOf(value).trim();
    }
}

