package com.example.dbeaver.jinja.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.sql.SQLQueryTransformer;
import org.jkiss.dbeaver.model.sql.transformers.SQLQueryTransformerAllRows;
import org.jkiss.dbeaver.model.sql.transformers.SQLQueryTransformerCount;
import org.jkiss.dbeaver.model.sql.transformers.SQLQueryTransformerExpression;
import org.jkiss.dbeaver.ui.editors.sql.SQLEditor;
import org.jkiss.dbeaver.utils.RuntimeUtils;

public final class JinjaExecuteHandler extends AbstractHandler {
    private static final Log LOG = Log.getLog(JinjaExecuteHandler.class);

    private final JinjaExecutionService executionService = new JinjaExecutionService();
    private final SQLEditorBridge editorBridge = new SQLEditorBridge();

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        SQLEditor editor = RuntimeUtils.getObjectAdapter(HandlerUtil.getActiveEditor(event), SQLEditor.class);
        if (editor == null) {
            LOG.error("No active SQL editor found");
            return null;
        }

        ExecutionMode mode = ExecutionMode.fromCommandId(event.getCommand().getId());
        if (mode == null) {
            LOG.error("Unsupported SQL execution command: " + event.getCommand().getId());
            return null;
        }

        TemplatePreferenceStore.initializeDefaults();
        Shell shell = HandlerUtil.getActiveShell(event);

        try {
            String sqlToInspect = mode.extractSql(editor, editorBridge);
            JinjaExecutionService.PreparedExecution preparedExecution = executionService.prepareExecution(shell, sqlToInspect);
            if (preparedExecution.executionCancelled()) {
                return null;
            }
            if (preparedExecution.requiresTransformation()) {
                LOG.info("Executing SQL through Jinja transformer for command " + event.getCommand().getId());
            }

            if (preparedExecution.requiresTransformation() && mode == ExecutionMode.EXPLAIN_QUERY_PLAN) {
                MessageDialog.openInformation(
                    shell,
                    "Jinja Execution",
                    "Explain plan is not supported for Jinja templates in this MVP. Use Run Statement or preview the rendered SQL first."
                );
                return null;
            }
            if (preparedExecution.requiresTransformation() && mode == ExecutionMode.LOAD_QUERY_PLAN) {
                MessageDialog.openInformation(
                    shell,
                    "Jinja Execution",
                    "Load query plan is not supported for Jinja templates in this MVP. Use Run Statement or preview the rendered SQL first."
                );
                return null;
            }

            SQLQueryTransformer transformer = executionService.composeTransformer(mode.createBaseTransformer(), preparedExecution);
            mode.execute(editor, transformer);
            editor.refreshActions();
            return null;
        } catch (Exception ex) {
            throw new ExecutionException("Failed to execute Jinja-aware SQL command", ex);
        }
    }

    private enum ExecutionMode {
        RUN_STATEMENT("org.jkiss.dbeaver.ui.editors.sql.run.statement") {
            @Override
            String extractSql(SQLEditor editor, SQLEditorBridge bridge) {
                return sqlFromActiveQuery(editor, bridge);
            }

            @Override
            void execute(SQLEditor editor, SQLQueryTransformer transformer) {
                editor.processSQL(false, false, transformer, null);
            }
        },
        RUN_STATEMENT_NEW("org.jkiss.dbeaver.ui.editors.sql.run.statementNew") {
            @Override
            String extractSql(SQLEditor editor, SQLEditorBridge bridge) {
                return sqlFromActiveQuery(editor, bridge);
            }

            @Override
            void execute(SQLEditor editor, SQLQueryTransformer transformer) {
                editor.processSQL(true, false, transformer, null);
            }
        },
        RUN_SCRIPT("org.jkiss.dbeaver.ui.editors.sql.run.script") {
            @Override
            String extractSql(SQLEditor editor, SQLEditorBridge bridge) throws Exception {
                return bridge.read(editor).text();
            }

            @Override
            void execute(SQLEditor editor, SQLQueryTransformer transformer) {
                editor.processSQL(false, true, transformer, null);
            }
        },
        RUN_SCRIPT_FROM_POSITION("org.jkiss.dbeaver.ui.editors.sql.run.scriptFromPosition") {
            @Override
            String extractSql(SQLEditor editor, SQLEditorBridge bridge) throws Exception {
                return bridge.read(editor).text();
            }

            @Override
            void execute(SQLEditor editor, SQLQueryTransformer transformer) {
                editor.processSQL(false, true, true, transformer, null);
            }
        },
        RUN_SCRIPT_NEW("org.jkiss.dbeaver.ui.editors.sql.run.scriptNew") {
            @Override
            String extractSql(SQLEditor editor, SQLEditorBridge bridge) throws Exception {
                return bridge.read(editor).text();
            }

            @Override
            void execute(SQLEditor editor, SQLQueryTransformer transformer) {
                editor.processSQL(true, true, transformer, null);
            }
        },
        RUN_COUNT("org.jkiss.dbeaver.ui.editors.sql.run.count") {
            @Override
            String extractSql(SQLEditor editor, SQLEditorBridge bridge) {
                return sqlFromActiveQuery(editor, bridge);
            }

            @Override
            SQLQueryTransformer createBaseTransformer() {
                return new SQLQueryTransformerCount();
            }

            @Override
            void execute(SQLEditor editor, SQLQueryTransformer transformer) {
                editor.processSQL(false, false, transformer, null);
            }
        },
        RUN_EXPRESSION("org.jkiss.dbeaver.ui.editors.sql.run.expression") {
            @Override
            String extractSql(SQLEditor editor, SQLEditorBridge bridge) throws Exception {
                return bridge.read(editor).text();
            }

            @Override
            SQLQueryTransformer createBaseTransformer() {
                return new SQLQueryTransformerExpression();
            }

            @Override
            void execute(SQLEditor editor, SQLQueryTransformer transformer) {
                editor.processSQL(false, false, transformer, null);
            }
        },
        RUN_ALL_ROWS("org.jkiss.dbeaver.ui.editors.sql.run.all.rows") {
            @Override
            String extractSql(SQLEditor editor, SQLEditorBridge bridge) {
                return sqlFromActiveQuery(editor, bridge);
            }

            @Override
            SQLQueryTransformer createBaseTransformer() {
                return new SQLQueryTransformerAllRows();
            }

            @Override
            void execute(SQLEditor editor, SQLQueryTransformer transformer) {
                editor.processSQL(false, false, transformer, null);
            }
        },
        EXPLAIN_QUERY_PLAN("org.jkiss.dbeaver.ui.editors.sql.run.explain") {
            @Override
            String extractSql(SQLEditor editor, SQLEditorBridge bridge) {
                return sqlFromActiveQuery(editor, bridge);
            }

            @Override
            void execute(SQLEditor editor, SQLQueryTransformer transformer) {
                editor.explainQueryPlan();
            }
        },
        LOAD_QUERY_PLAN("org.jkiss.dbeaver.ui.editors.sql.load.plan") {
            @Override
            String extractSql(SQLEditor editor, SQLEditorBridge bridge) {
                return sqlFromActiveQuery(editor, bridge);
            }

            @Override
            void execute(SQLEditor editor, SQLQueryTransformer transformer) {
                editor.loadQueryPlan();
            }
        };

        private final String commandId;

        ExecutionMode(String commandId) {
            this.commandId = commandId;
        }

        static ExecutionMode fromCommandId(String commandId) {
            for (ExecutionMode mode : values()) {
                if (mode.commandId.equals(commandId)) {
                    return mode;
                }
            }
            return null;
        }

        SQLQueryTransformer createBaseTransformer() {
            return null;
        }

        abstract String extractSql(SQLEditor editor, SQLEditorBridge bridge) throws Exception;

        abstract void execute(SQLEditor editor, SQLQueryTransformer transformer);

        static String sqlFromActiveQuery(SQLEditor editor, SQLEditorBridge bridge) {
            if (editor.extractActiveQuery() != null) {
                return editor.extractActiveQuery().getText();
            }
            try {
                return bridge.read(editor).text();
            } catch (Exception e) {
                return "";
            }
        }
    }
}
