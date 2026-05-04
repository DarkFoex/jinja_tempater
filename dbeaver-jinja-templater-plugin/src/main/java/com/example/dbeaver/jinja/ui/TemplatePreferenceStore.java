package com.example.dbeaver.jinja.ui;

import org.eclipse.jface.preference.IPreferenceStore;

public final class TemplatePreferenceStore {
    public static final String KEY_LAST_VARIABLES = "lastVariablesJson";
    public static final String KEY_STRICT_VARIABLES = "strictVariables";
    public static final String KEY_RENDER_MODE = "renderMode";
    public static final String KEY_SAVE_LAST_VARIABLES = "saveLastVariables";

    private TemplatePreferenceStore() {
    }

    public static void initializeDefaults() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        store.setDefault(KEY_LAST_VARIABLES, "{\n  \"schema\": \"public\"\n}");
        store.setDefault(KEY_STRICT_VARIABLES, true);
        store.setDefault(KEY_RENDER_MODE, RenderMode.REPLACE_SELECTION.name());
        store.setDefault(KEY_SAVE_LAST_VARIABLES, true);
    }

    public static String getLastVariablesJson() {
        return Activator.getDefault().getPreferenceStore().getString(KEY_LAST_VARIABLES);
    }

    public static boolean isStrictVariables() {
        return Activator.getDefault().getPreferenceStore().getBoolean(KEY_STRICT_VARIABLES);
    }

    public static RenderMode getRenderMode() {
        return RenderMode.fromPersisted(Activator.getDefault().getPreferenceStore().getString(KEY_RENDER_MODE));
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
