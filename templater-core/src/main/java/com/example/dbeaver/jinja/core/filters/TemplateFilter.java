package com.example.dbeaver.jinja.core.filters;

import java.util.List;

public interface TemplateFilter {
    Object apply(Object value, List<Object> arguments);
}

