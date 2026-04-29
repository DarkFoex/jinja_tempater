package com.example.dbeaver.jinja.core;

public class TemplateRenderException extends RuntimeException {
    private final String errorCode;
    private final int line;
    private final int column;

    public TemplateRenderException(String errorCode, String message) {
        this(errorCode, message, -1, -1, null);
    }

    public TemplateRenderException(String errorCode, String message, Throwable cause) {
        this(errorCode, message, -1, -1, cause);
    }

    public TemplateRenderException(String errorCode, String message, int line, int column) {
        this(errorCode, message, line, column, null);
    }

    public TemplateRenderException(String errorCode, String message, int line, int column, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.line = line;
        this.column = column;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}

