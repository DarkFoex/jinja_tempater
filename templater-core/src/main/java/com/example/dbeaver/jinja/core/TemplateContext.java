package com.example.dbeaver.jinja.core;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TemplateContext {
    private final Map<String, Object> rootVariables;
    private final Deque<Map<String, Object>> scopes = new ArrayDeque<>();
    private final RenderOptions options;

    public TemplateContext(Map<String, Object> rootVariables, RenderOptions options) {
        this.rootVariables = rootVariables == null ? Map.of() : new LinkedHashMap<>(rootVariables);
        this.options = options == null ? RenderOptions.strict() : options;
    }

    public RenderOptions getOptions() {
        return options;
    }

    public void pushScope(Map<String, Object> scope) {
        scopes.push(scope);
    }

    public void popScope() {
        scopes.pop();
    }

    public Object lookupRoot(String name) {
        for (Map<String, Object> scope : scopes) {
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }
        if (rootVariables.containsKey(name)) {
            return rootVariables.get(name);
        }
        return null;
    }

    public boolean contains(String name) {
        for (Map<String, Object> scope : scopes) {
            if (scope.containsKey(name)) {
                return true;
            }
        }
        return rootVariables.containsKey(name);
    }
}
