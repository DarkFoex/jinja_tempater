package com.example.dbeaver.jinja.core.filters;

import java.util.List;
import java.util.Locale;

public final class LowerFilter implements TemplateFilter {
    @Override
    public Object apply(Object value, List<Object> arguments) {
        return value == null ? null : String.valueOf(value).toLowerCase(Locale.ROOT);
    }
}

