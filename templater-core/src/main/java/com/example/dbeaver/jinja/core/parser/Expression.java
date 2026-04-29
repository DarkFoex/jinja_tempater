package com.example.dbeaver.jinja.core.parser;

import com.example.dbeaver.jinja.core.TemplateContext;
import com.example.dbeaver.jinja.core.render.TemplateRenderer;

public interface Expression {
    Object evaluate(TemplateContext context, TemplateRenderer renderer);

    String asDebugString();
}

