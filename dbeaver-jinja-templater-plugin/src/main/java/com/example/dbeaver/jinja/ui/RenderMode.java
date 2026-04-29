package com.example.dbeaver.jinja.ui;

public enum RenderMode {
    PREVIEW_DIALOG,
    REPLACE_SELECTION,
    REPLACE_ALL;

    public static RenderMode fromPersisted(String value) {
        if (value == null || value.isBlank()) {
            return PREVIEW_DIALOG;
        }
        try {
            if ("OPEN_NEW_EDITOR".equals(value)) {
                return PREVIEW_DIALOG;
            }
            return RenderMode.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return PREVIEW_DIALOG;
        }
    }
}
