package com.example.dbeaver.jinja.core.filters;

import com.example.dbeaver.jinja.core.TemplateRenderException;

import java.util.LinkedHashMap;
import java.util.Map;

public final class FilterRegistry {
    private final Map<String, TemplateFilter> filters = new LinkedHashMap<>();

    public static FilterRegistry createDefault() {
        FilterRegistry registry = new FilterRegistry();
        registry.register("upper", new UpperFilter());
        registry.register("lower", new LowerFilter());
        registry.register("trim", new TrimFilter());
        registry.register("default", new DefaultFilter());
        registry.register("join", new JoinFilter());
        registry.register("sql_string", new SqlStringFilter());
        return registry;
    }

    public void register(String name, TemplateFilter filter) {
        filters.put(name, filter);
    }

    public TemplateFilter get(String name) {
        TemplateFilter filter = filters.get(name);
        if (filter == null) {
            throw new TemplateRenderException("UNKNOWN_FILTER", "Unknown filter: " + name);
        }
        return filter;
    }
}

