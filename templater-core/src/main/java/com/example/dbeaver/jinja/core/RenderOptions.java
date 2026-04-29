package com.example.dbeaver.jinja.core;

public final class RenderOptions {
    private final boolean strictVariables;

    public RenderOptions(boolean strictVariables) {
        this.strictVariables = strictVariables;
    }

    public static RenderOptions strict() {
        return new RenderOptions(true);
    }

    public static RenderOptions lenient() {
        return new RenderOptions(false);
    }

    public boolean isStrictVariables() {
        return strictVariables;
    }
}

