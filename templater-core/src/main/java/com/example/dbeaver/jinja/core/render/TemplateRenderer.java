package com.example.dbeaver.jinja.core.render;

import com.example.dbeaver.jinja.core.TemplateContext;
import com.example.dbeaver.jinja.core.filters.FilterRegistry;
import com.example.dbeaver.jinja.core.filters.TemplateFilter;
import com.example.dbeaver.jinja.core.ast.Node;

import java.util.Collection;
import java.util.List;

public final class TemplateRenderer {
    private final FilterRegistry filterRegistry;
    private final ValueResolver valueResolver = new ValueResolver();

    public TemplateRenderer(FilterRegistry filterRegistry) {
        this.filterRegistry = filterRegistry;
    }

    public String render(List<Node> nodes, TemplateContext context) {
        StringBuilder out = new StringBuilder();
        renderInto(nodes, context, out);
        return out.toString();
    }

    public void renderInto(List<Node> nodes, TemplateContext context, StringBuilder out) {
        for (Node node : nodes) {
            node.render(context, this, out);
        }
    }

    public Object resolvePath(TemplateContext context, String path) {
        return valueResolver.resolve(context, path);
    }

    public Object applyFilter(String filterName, Object value, List<Object> arguments) {
        TemplateFilter filter = filterRegistry.get(filterName);
        return filter.apply(value, arguments);
    }

    public boolean isTruthy(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof CharSequence chars) {
            return chars.length() > 0;
        }
        if (value instanceof Collection<?> collection) {
            return !collection.isEmpty();
        }
        if (value instanceof Number number) {
            return number.doubleValue() != 0D;
        }
        return true;
    }

    public String stringify(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(this::stringify).reduce((a, b) -> a + "," + b).orElse("");
        }
        return String.valueOf(value);
    }
}

