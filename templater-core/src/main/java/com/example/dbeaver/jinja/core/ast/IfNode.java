package com.example.dbeaver.jinja.core.ast;

import com.example.dbeaver.jinja.core.TemplateContext;
import com.example.dbeaver.jinja.core.parser.Condition;
import com.example.dbeaver.jinja.core.render.TemplateRenderer;

import java.util.List;

public final class IfNode implements Node {
    private final Condition condition;
    private final List<Node> children;

    public IfNode(Condition condition, List<Node> children) {
        this.condition = condition;
        this.children = children;
    }

    @Override
    public void render(TemplateContext context, TemplateRenderer renderer, StringBuilder out) {
        if (condition.evaluate(context, renderer)) {
            renderer.renderInto(children, context, out);
        }
    }
}

