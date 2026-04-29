package com.example.dbeaver.jinja.ui;

import com.example.dbeaver.jinja.core.RenderOptions;
import com.example.dbeaver.jinja.core.TemplateEngine;
import com.example.dbeaver.jinja.core.TemplateRenderException;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import java.util.Map;

public final class TemplateRenderCommandHandler extends AbstractHandler {
    private final TemplateEngine engine = new TemplateEngine();
    private final SQLEditorBridge editorBridge = new SQLEditorBridge();

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        TemplatePreferenceStore.initializeDefaults();
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
        IWorkbenchPage page = window == null ? null : window.getActivePage();
        IEditorPart editorPart = page == null ? null : page.getActiveEditor();
        Shell shell = HandlerUtil.getActiveShell(event);

        if (editorPart == null || !editorBridge.isSupported(editorPart)) {
            MessageDialog.openError(shell, "Render Jinja Template", "Active editor is not a DBeaver SQL Editor.");
            return null;
        }

        try {
            SQLEditorBridge.EditorText editorText = editorBridge.read(editorPart);
            TemplateVariableDialog dialog = new TemplateVariableDialog(
                shell,
                TemplatePreferenceStore.getLastVariablesJson(),
                TemplatePreferenceStore.isSaveLastVariables()
            );
            if (dialog.open() != Window.OK) {
                return null;
            }

            String variablesJson = dialog.getVariablesJson();
            boolean saveLastVariables = dialog.isSaveLastVariables();
            if (saveLastVariables) {
                TemplatePreferenceStore.saveLastVariablesJson(variablesJson);
            }
            TemplatePreferenceStore.saveLastVariablesEnabled(saveLastVariables);

            Map<String, Object> variables = engine.parseVariablesJson(variablesJson);
            String rendered = engine.render(
                editorText.text(),
                variables,
                new RenderOptions(TemplatePreferenceStore.isStrictVariables())
            );

            applyRenderedOutput(shell, editorPart, editorText, rendered);
            return null;
        } catch (TemplateRenderException ex) {
            MessageDialog.openError(shell, "Render Jinja Template", buildErrorMessage(ex));
            return null;
        } catch (Exception ex) {
            throw new ExecutionException("Failed to render Jinja template", ex);
        }
    }

    private void applyRenderedOutput(
        Shell shell,
        IEditorPart editorPart,
        SQLEditorBridge.EditorText editorText,
        String rendered
    ) throws Exception {
        RenderMode renderMode = TemplatePreferenceStore.getRenderMode();
        if (renderMode == RenderMode.PREVIEW_DIALOG) {
            new RenderedSqlDialog(shell, rendered).open();
            return;
        }
        editorBridge.applyRenderedText(editorPart, editorText, rendered, renderMode);
    }

    private String buildErrorMessage(TemplateRenderException ex) {
        StringBuilder message = new StringBuilder(ex.getMessage());
        if (ex.getLine() > 0) {
            message.append("\nLine: ").append(ex.getLine());
        }
        if (ex.getColumn() > 0) {
            message.append(", Column: ").append(ex.getColumn());
        }
        message.append("\nError code: ").append(ex.getErrorCode());
        return message.toString();
    }
}
