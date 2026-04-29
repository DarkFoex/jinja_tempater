package com.example.dbeaver.jinja.core.filters;

import java.util.List;
import java.util.Locale;

public final class UpperFilter implements TemplateFilter {
    @Override
    public Object apply(Object value, List<Object> arguments) {
        return value == null ? null : String.valueOf(value).toUpperCase(Locale.ROOT);
    }
}

