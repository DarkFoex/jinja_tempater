package com.example.dbeaver.jinja.core.parser;

public record Token(TokenType type, String content, int line, int column) {
}

