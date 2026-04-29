package com.example.dbeaver.jinja.core.filters;

import com.example.dbeaver.jinja.core.TemplateRenderException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public final class JoinFilter implements TemplateFilter {
    @Override
    public Object apply(Object value, List<Object> arguments) {
        String delimiter = arguments.isEmpty() ? "," : String.valueOf(arguments.get(0));
        if (value == null) {
            return "";
        }
        if (value instanceof Iterable<?> iterable) {
            List<String> values = new ArrayList<>();
            for (Object item : iterable) {
                values.add(item == null ? "" : String.valueOf(item));
            }
            return String.join(delimiter, values);
        }
        if (value.getClass().isArray()) {
            List<String> values = new ArrayList<>();
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                Object item = Array.get(value, i);
                values.add(item == null ? "" : String.valueOf(item));
            }
            return String.join(delimiter, values);
        }
        throw new TemplateRenderException("JOIN_FILTER", "join filter expects iterable or array");
    }
}

