package com.example.dbeaver.jinja.core.ast;

import com.example.dbeaver.jinja.core.TemplateContext;
import com.example.dbeaver.jinja.core.render.TemplateRenderer;

public final class TextNode implements Node {
    private final String text;

    public TextNode(String text) {
        this.text = text;
    }

    @Override
    public void render(TemplateContext context, TemplateRenderer renderer, StringBuilder out) {
        out.append(text);
    }
}

