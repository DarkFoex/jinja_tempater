package com.example.dbeaver.jinja.core.json;

import com.example.dbeaver.jinja.core.TemplateRenderException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JsonParser {
    private List<String> tokens;
    private int index;

    public Object parse(String source) {
        this.tokens = new JsonTokenizer().tokenize(source == null ? "" : source);
        this.index = 0;
        Object value = parseValue();
        if (index != tokens.size()) {
            throw new TemplateRenderException("JSON_TRAILING", "Unexpected trailing JSON tokens");
        }
        return value;
    }

    private Object parseValue() {
        String token = peek();
        if (token == null) {
            throw new TemplateRenderException("JSON_EMPTY", "JSON is empty");
        }
        return switch (token) {
            case "{" -> parseObject();
            case "[" -> parseArray();
            case "true" -> {
                index++;
                yield Boolean.TRUE;
            }
            case "false" -> {
                index++;
                yield Boolean.FALSE;
            }
            case "null" -> {
                index++;
                yield null;
            }
            default -> {
                index++;
                if (token.startsWith("\"") && token.endsWith("\"")) {
                    yield token.substring(1, token.length() - 1);
                }
                if (token.matches("-?\\d+")) {
                    yield Long.parseLong(token);
                }
                if (token.matches("-?\\d+\\.\\d+")) {
                    yield Double.parseDouble(token);
                }
                throw new TemplateRenderException("JSON_TOKEN", "Unexpected JSON token: " + token);
            }
        };
    }

    private Map<String, Object> parseObject() {
        expect("{");
        Map<String, Object> result = new LinkedHashMap<>();
        if (consume("}")) {
            return result;
        }
        do {
            String keyToken = next();
            if (keyToken == null || !keyToken.startsWith("\"") || !keyToken.endsWith("\"")) {
                throw new TemplateRenderException("JSON_KEY", "JSON object keys must be strings");
            }
            String key = keyToken.substring(1, keyToken.length() - 1);
            expect(":");
            result.put(key, parseValue());
        } while (consume(","));
        expect("}");
        return result;
    }

    private List<Object> parseArray() {
        expect("[");
        List<Object> result = new ArrayList<>();
        if (consume("]")) {
            return result;
        }
        do {
            result.add(parseValue());
        } while (consume(","));
        expect("]");
        return result;
    }

    private boolean consume(String token) {
        if (token.equals(peek())) {
            index++;
            return true;
        }
        return false;
    }

    private void expect(String token) {
        String actual = next();
        if (!token.equals(actual)) {
            throw new TemplateRenderException("JSON_EXPECTED", "Expected '" + token + "' but found '" + actual + "'");
        }
    }

    private String peek() {
        return index >= tokens.size() ? null : tokens.get(index);
    }

    private String next() {
        return index >= tokens.size() ? null : tokens.get(index++);
    }
}

