package com.example.dbeaver.jinja.ui;

public enum RenderMode {
    PREVIEW_DIALOG,
    REPLACE_SELECTION,
    REPLACE_ALL;

    public static RenderMode fromPersisted(String value) {
        if (value == null || value.isBlank()) {
            return REPLACE_SELECTION;
        }
        try {
            if ("OPEN_NEW_EDITOR".equals(value)) {
                return REPLACE_SELECTION;
            }
            return RenderMode.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return REPLACE_SELECTION;
        }
    }
}
