package com.example.dbeaver.jinja.core.json;

import com.example.dbeaver.jinja.core.TemplateRenderException;

import java.util.ArrayList;
import java.util.List;

public final class JsonTokenizer {
    public List<String> tokenize(String source) {
        List<String> tokens = new ArrayList<>();
        int index = 0;
        while (index < source.length()) {
            char ch = source.charAt(index);
            if (Character.isWhitespace(ch)) {
                index++;
                continue;
            }
            if ("{}[]:,".indexOf(ch) >= 0) {
                tokens.add(String.valueOf(ch));
                index++;
                continue;
            }
            if (ch == '"' || ch == '\'') {
                StringBuilder text = new StringBuilder();
                char quote = ch;
                index++;
                while (index < source.length()) {
                    char current = source.charAt(index++);
                    if (current == '\\') {
                        if (index >= source.length()) {
                            throw new TemplateRenderException("JSON_ESCAPE", "Invalid JSON escape sequence");
                        }
                        char escaped = source.charAt(index++);
                        text.append(switch (escaped) {
                            case '"', '\'', '\\', '/' -> escaped;
                            case 'b' -> '\b';
                            case 'f' -> '\f';
                            case 'n' -> '\n';
                            case 'r' -> '\r';
                            case 't' -> '\t';
                            default -> throw new TemplateRenderException("JSON_ESCAPE", "Unsupported JSON escape: \\" + escaped);
                        });
                    } else if (current == quote) {
                        break;
                    } else {
                        text.append(current);
                    }
                }
                tokens.add('"' + text.toString() + '"');
                continue;
            }
            int start = index;
            while (index < source.length() && !Character.isWhitespace(source.charAt(index)) && "{}[]:,".indexOf(source.charAt(index)) < 0) {
                index++;
            }
            tokens.add(source.substring(start, index));
        }
        return tokens;
    }
}

