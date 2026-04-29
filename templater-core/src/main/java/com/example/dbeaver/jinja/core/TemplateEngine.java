package com.example.dbeaver.jinja.core;

import com.example.dbeaver.jinja.core.ast.Node;
import com.example.dbeaver.jinja.core.filters.FilterRegistry;
import com.example.dbeaver.jinja.core.json.JsonParser;
import com.example.dbeaver.jinja.core.parser.TemplateParser;
import com.example.dbeaver.jinja.core.parser.TemplateTokenizer;
import com.example.dbeaver.jinja.core.render.TemplateRenderer;

import java.util.List;
import java.util.Map;

public final class TemplateEngine {
    private final FilterRegistry filterRegistry;

    public TemplateEngine() {
        this.filterRegistry = FilterRegistry.createDefault();
    }

    public String render(String template, Map<String, Object> variables, RenderOptions options) {
        List<Node> nodes = new TemplateParser().parse(new TemplateTokenizer().tokenize(template == null ? "" : template));
        TemplateContext context = new TemplateContext(variables, options);
        return new TemplateRenderer(filterRegistry).render(nodes, context);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> parseVariablesJson(String json) {
        Object parsed = new JsonParser().parse(json == null ? "{}" : json);
        if (!(parsed instanceof Map<?, ?> map)) {
            throw new TemplateRenderException("JSON_ROOT", "Variables JSON root must be an object");
        }
        return (Map<String, Object>) map;
    }
}

