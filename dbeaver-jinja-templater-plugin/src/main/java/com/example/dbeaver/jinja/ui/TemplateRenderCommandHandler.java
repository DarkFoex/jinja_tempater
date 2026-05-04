package com.example.dbeaver.jinja.ui;

import com.example.dbeaver.jinja.core.RenderOptions;
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
    private static final String DIALOG_TITLE = "Jinja Variables / Preview";

    private final JinjaExecutionService executionService = new JinjaExecutionService();
    private final SQLEditorBridge editorBridge = new SQLEditorBridge();

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        TemplatePreferenceStore.initializeDefaults();
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
        IWorkbenchPage page = window == null ? null : window.getActivePage();
        IEditorPart editorPart = page == null ? null : page.getActiveEditor();
        Shell shell = HandlerUtil.getActiveShell(event);

        if (editorPart == null || !editorBridge.isSupported(editorPart)) {
            MessageDialog.openError(shell, DIALOG_TITLE, "Active editor is not a DBeaver SQL Editor.");
            return null;
        }

        try {
            SQLEditorBridge.EditorText editorText = editorBridge.read(editorPart);
            TemplateVariableDialog dialog = new TemplateVariableDialog(
                shell,
                TemplatePreferenceStore.getInitialDialogJson(),
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

            Map<String, Object> variables = executionService.parseVariablesJson(variablesJson);
            String rendered = executionService.renderWithParsedVariables(
                editorText.text(),
                variables,
                new RenderOptions(TemplatePreferenceStore.isStrictVariables())
            );
            new RenderedSqlDialog(shell, rendered).open();
            return null;
        } catch (TemplateRenderException ex) {
            MessageDialog.openError(shell, DIALOG_TITLE, buildErrorMessage(ex));
            return null;
        } catch (Exception ex) {
            throw new ExecutionException("Failed to preview Jinja template", ex);
        }
    }

    private String buildErrorMessage(TemplateRenderException ex) {
        return executionService.buildErrorMessage(ex);
    }
}
