package com.example.dbeaver.jinja.core.parser;

import com.example.dbeaver.jinja.core.TemplateRenderException;

import java.util.ArrayList;
import java.util.List;

public final class TemplateTokenizer {
    public List<Token> tokenize(String template) {
        List<Token> tokens = new ArrayList<>();
        StringBuilder text = new StringBuilder();
        int line = 1;
        int column = 1;
        int textLine = line;
        int textColumn = column;
        int i = 0;

        while (i < template.length()) {
            if (startsWith(template, i, "{{") || startsWith(template, i, "{%") || startsWith(template, i, "{#")) {
                if (!text.isEmpty()) {
                    tokens.add(new Token(TokenType.TEXT, text.toString(), textLine, textColumn));
                    text.setLength(0);
                }
                String open = template.substring(i, i + 2);
                String close = switch (open) {
                    case "{{" -> "}}";
                    case "{%" -> "%}";
                    default -> "#}";
                };
                int tokenLine = line;
                int tokenColumn = column;
                i += 2;
                column += 2;

                int end = template.indexOf(close, i);
                if (end < 0) {
                    throw new TemplateRenderException("UNCLOSED_TAG", "Unclosed template token", tokenLine, tokenColumn);
                }
                String content = template.substring(i, end).trim();
                String raw = template.substring(i, end + 2);
                for (int k = 0; k < raw.length(); k++) {
                    char ch = raw.charAt(k);
                    if (ch == '\n') {
                        line++;
                        column = 1;
                    } else {
                        column++;
                    }
                }
                tokens.add(new Token(resolveType(open), content, tokenLine, tokenColumn));
                i = end + 2;
                textLine = line;
                textColumn = column;
            } else {
                char ch = template.charAt(i);
                if (text.isEmpty()) {
                    textLine = line;
                    textColumn = column;
                }
                text.append(ch);
                if (ch == '\n') {
                    line++;
                    column = 1;
                } else {
                    column++;
                }
                i++;
            }
        }

        if (!text.isEmpty()) {
            tokens.add(new Token(TokenType.TEXT, text.toString(), textLine, textColumn));
        }
        return tokens;
    }

    private boolean startsWith(String template, int index, String marker) {
        return index + marker.length() <= template.length() && template.startsWith(marker, index);
    }

    private TokenType resolveType(String marker) {
        return switch (marker) {
            case "{{" -> TokenType.VARIABLE;
            case "{%" -> TokenType.TAG;
            default -> TokenType.COMMENT;
        };
    }
}

