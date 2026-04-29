package com.example.dbeaver.jinja.core;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TemplateEngineTest {
    private final TemplateEngine engine = new TemplateEngine();

    @Test
    void variableRendering() {
        assertEquals("hello Alex", render("hello {{ name }}", Map.of("name", "Alex")));
    }

    @Test
    void nestedVariableRendering() {
        assertEquals("Alex", render("{{ user.name }}", Map.of("user", Map.of("name", "Alex"))));
    }

    @Test
    void ifTrue() {
        assertEquals("yes", render("{% if enabled %}yes{% endif %}", Map.of("enabled", true)));
    }

    @Test
    void ifFalse() {
        assertEquals("", render("{% if enabled %}yes{% endif %}", Map.of("enabled", false)));
    }

    @Test
    void ifNot() {
        assertEquals("yes", render("{% if not enabled %}yes{% endif %}", Map.of("enabled", false)));
    }

    @Test
    void comparison() {
        assertEquals("dev", render("{% if env == 'dev' %}dev{% endif %}", Map.of("env", "dev")));
    }

    @Test
    void forLoop() {
        assertEquals("ab", render("{% for item in items %}{{ item }}{% endfor %}", Map.of("items", List.of("a", "b"))));
    }

    @Test
    void commentsRemoved() {
        assertEquals("ab", render("a{# hidden #}b", Map.of()));
    }

    @Test
    void filters() {
        assertEquals("ALEX", render("{{ name | upper }}", Map.of("name", "Alex")));
        assertEquals("alex", render("{{ name | lower }}", Map.of("name", "Alex")));
        assertEquals("Alex", render("{{ name | trim }}", Map.of("name", "  Alex  ")));
        assertEquals("fallback", render("{{ missing | default('fallback') }}", Map.of(), false));
        assertEquals("a, b", render("{{ items | join(', ') }}", Map.of("items", List.of("a", "b"))));
        assertEquals("'Alex''s test'", render("{{ name | sql_string }}", Map.of("name", "Alex's test")));
        Map<String, Object> varsWithNull = new LinkedHashMap<>();
        varsWithNull.put("value", null);
        assertEquals("NULL", render("{{ value | sql_string }}", varsWithNull, false));
    }

    @Test
    void unclosedIf() {
        TemplateRenderException ex = assertThrows(TemplateRenderException.class, () -> render("{% if enabled %}x", Map.of("enabled", true)));
        assertEquals("UNCLOSED_IF", ex.getErrorCode());
    }

    @Test
    void unclosedFor() {
        TemplateRenderException ex = assertThrows(TemplateRenderException.class, () -> render("{% for item in items %}x", Map.of("items", List.of("x"))));
        assertEquals("UNCLOSED_FOR", ex.getErrorCode());
    }

    @Test
    void unknownFilter() {
        TemplateRenderException ex = assertThrows(TemplateRenderException.class, () -> render("{{ name | nope }}", Map.of("name", "Alex")));
        assertEquals("UNKNOWN_FILTER", ex.getErrorCode());
    }

    @Test
    void strictUnknownVariable() {
        TemplateRenderException ex = assertThrows(TemplateRenderException.class, () -> render("{{ missing }}", Map.of()));
        assertEquals("UNKNOWN_VARIABLE", ex.getErrorCode());
    }

    @Test
    void lenientUnknownVariable() {
        assertEquals("", render("{{ missing }}", Map.of(), false));
    }

    @Test
    void strictKnownNullVariableWithSqlString() {
        Map<String, Object> varsWithNull = new LinkedHashMap<>();
        varsWithNull.put("value", null);
        assertEquals("NULL", render("{{ value | sql_string }}", varsWithNull));
    }

    @Test
    void invalidJsonParsing() {
        assertThrows(TemplateRenderException.class, () -> engine.parseVariablesJson("{invalid"));
    }

    private String render(String template, Map<String, Object> variables) {
        return render(template, variables, true);
    }

    private String render(String template, Map<String, Object> variables, boolean strict) {
        return engine.render(template, variables, new RenderOptions(strict));
    }
}
