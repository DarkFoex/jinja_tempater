package com.example.dbeaver.jinja.core.parser;

import com.example.dbeaver.jinja.core.TemplateRenderException;
import com.example.dbeaver.jinja.core.ast.ForNode;
import com.example.dbeaver.jinja.core.ast.IfNode;
import com.example.dbeaver.jinja.core.ast.Node;
import com.example.dbeaver.jinja.core.ast.TextNode;
import com.example.dbeaver.jinja.core.ast.VariableNode;
import com.example.dbeaver.jinja.core.parser.VariableExpression.FilterInvocation;

import java.util.ArrayList;
import java.util.List;

public final class TemplateParser {
    private List<Token> tokens;
    private int index;

    public List<Node> parse(List<Token> tokens) {
        this.tokens = tokens;
        this.index = 0;
        return parseUntil(null);
    }

    private List<Node> parseUntil(String expectedEndTag) {
        List<Node> nodes = new ArrayList<>();
        while (index < tokens.size()) {
            Token token = tokens.get(index++);
            switch (token.type()) {
                case TEXT -> nodes.add(new TextNode(token.content()));
                case COMMENT -> {
                    // Comments are intentionally dropped.
                }
                case VARIABLE -> nodes.add(new VariableNode(parseExpression(token.content())));
                case TAG -> {
                    String content = token.content();
                    if (content.startsWith("if ")) {
                        Condition condition = parseCondition(content.substring(3).trim(), token);
                        List<Node> children = parseUntil("endif");
                        nodes.add(new IfNode(condition, children));
                    } else if (content.startsWith("for ")) {
                        nodes.add(parseForNode(content.substring(4).trim(), token));
                    } else if ("endif".equals(content) || "endfor".equals(content)) {
                        if (expectedEndTag == null) {
                            throw new TemplateRenderException("UNEXPECTED_END", "Unexpected closing tag: " + content, token.line(), token.column());
                        }
                        if (!expectedEndTag.equals(content)) {
                            throw new TemplateRenderException("MISMATCHED_END", "Expected " + expectedEndTag + " but found " + content, token.line(), token.column());
                        }
                        return nodes;
                    } else {
                        throw new TemplateRenderException("UNKNOWN_TAG", "Unknown tag: " + content, token.line(), token.column());
                    }
                }
            }
        }
        if (expectedEndTag != null) {
            String code = "endif".equals(expectedEndTag) ? "UNCLOSED_IF" : "UNCLOSED_FOR";
            throw new TemplateRenderException(code, "Missing closing tag: " + expectedEndTag);
        }
        return nodes;
    }

    private ForNode parseForNode(String body, Token token) {
        int inIndex = body.indexOf(" in ");
        if (inIndex <= 0) {
            throw new TemplateRenderException("MALFORMED_FOR", "Malformed for tag: " + body, token.line(), token.column());
        }
        String variableName = body.substring(0, inIndex).trim();
        if (!variableName.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new TemplateRenderException("MALFORMED_FOR", "Invalid loop variable: " + variableName, token.line(), token.column());
        }
        Expression iterable = parseExpression(body.substring(inIndex + 4).trim());
        List<Node> children = parseUntil("endfor");
        return new ForNode(variableName, iterable, children);
    }

    private Condition parseCondition(String source, Token token) {
        boolean negated = false;
        String expression = source;
        if (expression.startsWith("not ")) {
            negated = true;
            expression = expression.substring(4).trim();
        }
        int eqIndex = indexOfOperator(expression, "==");
        if (eqIndex >= 0) {
            Expression left = parseExpression(expression.substring(0, eqIndex).trim());
            Expression right = parseExpression(expression.substring(eqIndex + 2).trim());
            return new Condition(negated, left, right);
        }
        if (expression.isEmpty()) {
            throw new TemplateRenderException("MALFORMED_IF", "Malformed if expression", token.line(), token.column());
        }
        return new Condition(negated, parseExpression(expression), null);
    }

    private Expression parseExpression(String source) {
        List<String> pipeline = splitTopLevel(source, '|');
        Expression base = parseAtomicExpression(pipeline.get(0).trim());
        if (!(base instanceof VariableExpression variableExpression) || pipeline.size() == 1) {
            if (pipeline.size() > 1) {
                throw new TemplateRenderException("FILTER_BASE", "Filters can only be applied to variable expressions in v1: " + source);
            }
            return base;
        }

        List<FilterInvocation> filters = new ArrayList<>();
        for (int i = 1; i < pipeline.size(); i++) {
            String filterSource = pipeline.get(i).trim();
            String name = filterSource;
            List<Expression> args = List.of();
            int openParen = filterSource.indexOf('(');
            if (openParen >= 0) {
                int closeParen = filterSource.lastIndexOf(')');
                if (closeParen < openParen) {
                    throw new TemplateRenderException("MALFORMED_FILTER", "Malformed filter: " + filterSource);
                }
                name = filterSource.substring(0, openParen).trim();
                String argsSource = filterSource.substring(openParen + 1, closeParen).trim();
                args = parseArguments(argsSource);
            }
            filters.add(new FilterInvocation(name, args));
        }
        return new VariableExpression(variableExpression.asDebugString(), filters);
    }

    private List<Expression> parseArguments(String source) {
        if (source.isEmpty()) {
            return List.of();
        }
        List<String> raw = splitTopLevel(source, ',');
        List<Expression> expressions = new ArrayList<>(raw.size());
        for (String value : raw) {
            expressions.add(parseAtomicExpression(value.trim()));
        }
        return expressions;
    }

    private Expression parseAtomicExpression(String source) {
        if (source.isEmpty()) {
            throw new TemplateRenderException("EMPTY_EXPRESSION", "Expression is empty");
        }
        if ((source.startsWith("'") && source.endsWith("'")) || (source.startsWith("\"") && source.endsWith("\""))) {
            return new LiteralExpression(unescapeQuoted(source));
        }
        if ("true".equals(source)) {
            return new LiteralExpression(Boolean.TRUE);
        }
        if ("false".equals(source)) {
            return new LiteralExpression(Boolean.FALSE);
        }
        if ("null".equals(source)) {
            return new LiteralExpression(null);
        }
        if (source.matches("-?\\d+")) {
            return new LiteralExpression(Long.parseLong(source));
        }
        if (source.matches("-?\\d+\\.\\d+")) {
            return new LiteralExpression(Double.parseDouble(source));
        }
        if (!source.matches("[A-Za-z_][A-Za-z0-9_\\.]*")) {
            throw new TemplateRenderException("MALFORMED_EXPRESSION", "Unsupported expression: " + source);
        }
        return new VariableExpression(source, List.of());
    }

    private String unescapeQuoted(String source) {
        String quote = source.substring(0, 1);
        String body = source.substring(1, source.length() - 1);
        return body.replace("\\" + quote, quote).replace("\\\\", "\\");
    }

    private int indexOfOperator(String source, String operator) {
        boolean inQuote = false;
        char quote = 0;
        for (int i = 0; i <= source.length() - operator.length(); i++) {
            char ch = source.charAt(i);
            if ((ch == '\'' || ch == '"') && (i == 0 || source.charAt(i - 1) != '\\')) {
                if (!inQuote) {
                    inQuote = true;
                    quote = ch;
                } else if (quote == ch) {
                    inQuote = false;
                }
            }
            if (!inQuote && source.startsWith(operator, i)) {
                return i;
            }
        }
        return -1;
    }

    private List<String> splitTopLevel(String source, char delimiter) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;
        boolean inQuote = false;
        char quote = 0;

        for (int i = 0; i < source.length(); i++) {
            char ch = source.charAt(i);
            if ((ch == '\'' || ch == '"') && (i == 0 || source.charAt(i - 1) != '\\')) {
                if (!inQuote) {
                    inQuote = true;
                    quote = ch;
                } else if (quote == ch) {
                    inQuote = false;
                }
            } else if (!inQuote) {
                if (ch == '(') {
                    depth++;
                } else if (ch == ')') {
                    depth--;
                } else if (ch == delimiter && depth == 0) {
                    parts.add(current.toString());
                    current.setLength(0);
                    continue;
                }
            }
            current.append(ch);
        }
        parts.add(current.toString());
        return parts;
    }
}

