package com.example.dbeaver.jinja.core.ast;

import com.example.dbeaver.jinja.core.TemplateContext;
import com.example.dbeaver.jinja.core.TemplateRenderException;
import com.example.dbeaver.jinja.core.parser.Expression;
import com.example.dbeaver.jinja.core.render.TemplateRenderer;

import java.lang.reflect.Array;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ForNode implements Node {
    private final String variableName;
    private final Expression iterableExpression;
    private final List<Node> children;

    public ForNode(String variableName, Expression iterableExpression, List<Node> children) {
        this.variableName = variableName;
        this.iterableExpression = iterableExpression;
        this.children = children;
    }

    @Override
    public void render(TemplateContext context, TemplateRenderer renderer, StringBuilder out) {
        Object iterable = iterableExpression.evaluate(context, renderer);
        if (iterable == null) {
            return;
        }
        if (iterable instanceof Iterable<?> items) {
            for (Object item : items) {
                renderIteration(context, renderer, out, item);
            }
            return;
        }
        if (iterable.getClass().isArray()) {
            int length = Array.getLength(iterable);
            for (int i = 0; i < length; i++) {
                renderIteration(context, renderer, out, Array.get(iterable, i));
            }
            return;
        }
        throw new TemplateRenderException("FOR_ITERABLE", "Expression in for-loop is not iterable: " + iterableExpression.asDebugString());
    }

    private void renderIteration(TemplateContext context, TemplateRenderer renderer, StringBuilder out, Object item) {
        Map<String, Object> scope = new LinkedHashMap<>();
        scope.put(variableName, item);
        context.pushScope(scope);
        try {
            renderer.renderInto(children, context, out);
        } finally {
            context.popScope();
        }
    }
}

