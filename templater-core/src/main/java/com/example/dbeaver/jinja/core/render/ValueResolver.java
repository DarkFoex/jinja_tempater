package com.example.dbeaver.jinja.core.render;

import com.example.dbeaver.jinja.core.TemplateContext;
import com.example.dbeaver.jinja.core.TemplateRenderException;

import java.lang.reflect.Method;
import java.util.Map;

public final class ValueResolver {
    public Object resolve(TemplateContext context, String path) {
        String[] parts = path.split("\\.");
        if (!context.contains(parts[0])) {
            if (context.getOptions().isStrictVariables()) {
                throw new TemplateRenderException("UNKNOWN_VARIABLE", "Unknown variable: " + path);
            }
            return null;
        }
        Object current = context.lookupRoot(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            Resolution resolution = resolvePart(current, parts[i]);
            if (!resolution.resolved()) {
                if (context.getOptions().isStrictVariables()) {
                    throw new TemplateRenderException("UNKNOWN_VARIABLE", "Unknown variable: " + path);
                }
                return null;
            }
            current = resolution.value();
        }
        return current;
    }

    private Resolution resolvePart(Object current, String name) {
        if (current == null) {
            return Resolution.unresolved();
        }
        if (current instanceof Map<?, ?> map) {
            return map.containsKey(name) ? Resolution.resolved(map.get(name)) : Resolution.unresolved();
        }
        try {
            String suffix = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            Method getter = current.getClass().getMethod("get" + suffix);
            return Resolution.resolved(getter.invoke(current));
        } catch (Exception ignored) {
            // Fallback to no-arg method lookup below.
        }
        try {
            Method getter = current.getClass().getMethod(name);
            return Resolution.resolved(getter.invoke(current));
        } catch (Exception ignored) {
            return Resolution.unresolved();
        }
    }

    private record Resolution(boolean resolved, Object value) {
        private static Resolution resolved(Object value) {
            return new Resolution(true, value);
        }

        private static Resolution unresolved() {
            return new Resolution(false, null);
        }
    }
}
