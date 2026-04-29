package com.example.dbeaver.jinja.ui;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public final class SQLEditorBridge {
    public static final String SQL_EDITOR_ID = "org.jkiss.dbeaver.ui.editors.sql.SQLEditor";

    public boolean isSupported(IEditorPart editorPart) {
        return editorPart instanceof ITextEditor;
    }

    public EditorText read(IEditorPart editorPart) throws BadLocationException {
        ITextEditor editor = requireTextEditor(editorPart);
        IDocument document = requireDocument(editor);
        ISelection selection = getSelection(editor);
        if (selection instanceof ITextSelection textSelection && textSelection.getLength() > 0) {
            return new EditorText(textSelection.getText(), textSelection.getOffset(), textSelection.getLength(), false);
        }
        return new EditorText(document.get(), 0, document.getLength(), true);
    }

    public void applyRenderedText(IEditorPart editorPart, EditorText source, String rendered, RenderMode mode) throws BadLocationException {
        ITextEditor editor = requireTextEditor(editorPart);
        IDocument document = requireDocument(editor);
        switch (mode) {
            case REPLACE_SELECTION -> document.replace(source.offset(), source.length(), rendered);
            case REPLACE_ALL -> document.set(rendered);
            case PREVIEW_DIALOG -> {
                // Preview is handled by the command handler, not by the editor bridge.
            }
        }
    }

    private ITextEditor requireTextEditor(IEditorPart editorPart) {
        if (!(editorPart instanceof ITextEditor editor)) {
            throw new IllegalStateException("Active editor is not a text editor");
        }
        return editor;
    }

    private IDocument requireDocument(ITextEditor editor) {
        IDocumentProvider provider = editor.getDocumentProvider();
        return provider.getDocument(editor.getEditorInput());
    }

    private ISelection getSelection(ITextEditor editor) {
        ISelectionProvider provider = editor.getSelectionProvider();
        return provider == null ? null : provider.getSelection();
    }

    public record EditorText(String text, int offset, int length, boolean wholeDocument) {
    }
}
