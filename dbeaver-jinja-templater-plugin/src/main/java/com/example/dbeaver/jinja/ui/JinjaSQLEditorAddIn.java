package com.example.dbeaver.jinja.ui;

import org.jkiss.dbeaver.ui.editors.sql.SQLEditor;
import org.jkiss.dbeaver.ui.editors.sql.addins.SQLEditorAddIn;

import java.io.PrintWriter;

public final class JinjaSQLEditorAddIn implements SQLEditorAddIn {
    private SQLEditor editor;

    @Override
    public void init(SQLEditor editor) {
        this.editor = editor;
    }

    @Override
    public void cleanup(SQLEditor editor) {
        if (this.editor == editor) {
            this.editor = null;
        }
    }

    @Override
    public PrintWriter getServerOutputConsumer() {
        return null;
    }

    public SQLEditor getEditor() {
        return editor;
    }
}
