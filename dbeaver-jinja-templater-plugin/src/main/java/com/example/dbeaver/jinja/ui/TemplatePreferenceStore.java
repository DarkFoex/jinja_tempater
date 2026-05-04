package com.example.dbeaver.jinja.ui;

import org.eclipse.jface.preference.IPreferenceStore;

public final class TemplatePreferenceStore {
    public static final String KEY_LAST_VARIABLES = "lastVariablesJson";
    public static final String KEY_STRICT_VARIABLES = "strictVariables";
    public static final String KEY_SAVE_LAST_VARIABLES = "saveLastVariables";
    private static final String EXAMPLE_JSON = """
        {
          "date_from": "2026-01-01",
          "date_to": "2026-01-31",
          "schema": "public",
          "tables": ["orders", "payments"],
          "env": "dev",
          "user": {
            "name": "Alex"
          }
        }
        """;

    private TemplatePreferenceStore() {
    }

    public static void initializeDefaults() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        store.setDefault(KEY_LAST_VARIABLES, "");
        store.setDefault(KEY_STRICT_VARIABLES, true);
        store.setDefault(KEY_SAVE_LAST_VARIABLES, true);
    }

    public static String getLastVariablesJson() {
        return Activator.getDefault().getPreferenceStore().getString(KEY_LAST_VARIABLES);
    }

    public static String getInitialDialogJson() {
        String value = getLastVariablesJson();
        return value == null || value.isBlank() ? EXAMPLE_JSON : value;
    }

    public static String getExampleJson() {
        return EXAMPLE_JSON;
    }

    public static boolean hasSavedVariablesJson() {
        String value = getLastVariablesJson();
        return value != null && !value.isBlank();
    }

    public static boolean isStrictVariables() {
        return Activator.getDefault().getPreferenceStore().getBoolean(KEY_STRICT_VARIABLES);
    }

    public static boolean isSaveLastVariables() {
        return Activator.getDefault().getPreferenceStore().getBoolean(KEY_SAVE_LAST_VARIABLES);
    }

    public static void saveLastVariablesJson(String value) {
        Activator.getDefault().getPreferenceStore().setValue(KEY_LAST_VARIABLES, value);
    }

    public static void saveLastVariablesEnabled(boolean value) {
        Activator.getDefault().getPreferenceStore().setValue(KEY_SAVE_LAST_VARIABLES, value);
    }
}
