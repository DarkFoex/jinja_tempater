package com.example.dbeaver.jinja.core.ast;

import com.example.dbeaver.jinja.core.TemplateContext;
import com.example.dbeaver.jinja.core.render.TemplateRenderer;

public interface Node {
    void render(TemplateContext context, TemplateRenderer renderer, StringBuilder out);
}

