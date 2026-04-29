package com.example.dbeaver.jinja.core.filters;

import java.util.List;

public final class SqlStringFilter implements TemplateFilter {
    @Override
    public Object apply(Object value, List<Object> arguments) {
        if (value == null) {
            return "NULL";
        }
        String text = String.valueOf(value).replace("'", "''");
        return "'" + text + "'";
    }
}

