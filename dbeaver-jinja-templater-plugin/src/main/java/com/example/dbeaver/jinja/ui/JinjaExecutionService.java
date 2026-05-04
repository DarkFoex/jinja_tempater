package com.example.dbeaver.jinja.ui;

import com.example.dbeaver.jinja.core.RenderOptions;
import com.example.dbeaver.jinja.core.TemplateEngine;
import com.example.dbeaver.jinja.core.TemplateRenderException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.sql.SQLQuery;
import org.jkiss.dbeaver.model.sql.SQLQueryTransformer;
import org.jkiss.dbeaver.model.sql.SQLSyntaxManager;

import java.util.Map;

public final class JinjaExecutionService {
    private final TemplateEngine engine = new TemplateEngine();

    public boolean containsTemplateSyntax(String sql) {
        if (sql == null || sql.isBlank()) {
            return false;
        }
        return sql.contains("{{") || sql.contains("{%") || sql.contains("{#");
    }

    public String renderForExecution(String sourceSql, String variablesJson, boolean strictVariables) {
        Map<String, Object> variables = parseVariablesJson(variablesJson);
        return renderWithParsedVariables(sourceSql, variables, new RenderOptions(strictVariables));
    }

    public Map<String, Object> parseVariablesJson(String variablesJson) {
        return engine.parseVariablesJson(variablesJson);
    }

    public String renderWithParsedVariables(String sourceSql, Map<String, Object> variables, RenderOptions options) {
        return engine.render(sourceSql, variables, options);
    }

    public String buildErrorMessage(TemplateRenderException ex) {
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

    public PreparedExecution prepareExecution(Shell shell, String sqlToInspect) {
        if (!containsTemplateSyntax(sqlToInspect)) {
            return PreparedExecution.noTransformation();
        }

        String variablesJson = TemplatePreferenceStore.getLastVariablesJson();
        if (variablesJson == null || variablesJson.isBlank()) {
            variablesJson = promptForVariables(shell);
            if (variablesJson == null) {
                return PreparedExecution.cancelledExecution();
            }
        } else {
            try {
                parseVariablesJson(variablesJson);
            } catch (TemplateRenderException ex) {
                MessageDialog.openError(shell, "Jinja Variables", buildErrorMessage(ex));
                variablesJson = promptForVariables(shell);
                if (variablesJson == null) {
                    return PreparedExecution.cancelledExecution();
                }
            }
        }

        return PreparedExecution.withVariables(variablesJson);
    }

    public SQLQueryTransformer composeTransformer(SQLQueryTransformer delegate, PreparedExecution preparedExecution) {
        if (preparedExecution == null || !preparedExecution.requiresTransformation()) {
            return delegate;
        }
        return new JinjaSqlQueryTransformer(
            preparedExecution.variablesJson(),
            TemplatePreferenceStore.isStrictVariables(),
            delegate
        );
    }

    private String promptForVariables(Shell shell) {
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

        parseVariablesJson(variablesJson);
        return variablesJson;
    }

    public record PreparedExecution(boolean executionCancelled, String variablesJson) {
        public static PreparedExecution noTransformation() {
            return new PreparedExecution(false, null);
        }

        public static PreparedExecution cancelledExecution() {
            return new PreparedExecution(true, null);
        }

        public static PreparedExecution withVariables(String variablesJson) {
            return new PreparedExecution(false, variablesJson);
        }

        public boolean requiresTransformation() {
            return variablesJson != null;
        }
    }

    private final class JinjaSqlQueryTransformer implements SQLQueryTransformer {
        private final String variablesJson;
        private final boolean strictVariables;
        private final SQLQueryTransformer delegate;

        private JinjaSqlQueryTransformer(String variablesJson, boolean strictVariables, SQLQueryTransformer delegate) {
            this.variablesJson = variablesJson;
            this.strictVariables = strictVariables;
            this.delegate = delegate;
        }

        private String variablesJson() {
            return variablesJson;
        }

        @Override
        public SQLQuery transformQuery(DBPDataSource dataSource, SQLSyntaxManager syntaxManager, SQLQuery query) throws DBException {
            SQLQuery effectiveQuery = query;
            if (containsTemplateSyntax(query.getText())) {
                try {
                    String renderedSql = renderForExecution(query.getText(), variablesJson, strictVariables);
                    SQLQuery transformedQuery = new SQLQuery(dataSource, renderedSql, query);
                    transformedQuery.setOriginalText(query.getOriginalText());
                    transformedQuery.setOffset(query.getOffset());
                    transformedQuery.setLength(query.getLength());
                    transformedQuery.setData(query.getData());
                    if (query.isEndsWithDelimiter() != null) {
                        transformedQuery.setEndsWithDelimiter(query.isEndsWithDelimiter());
                    }
                    if (query.getParameters() != null) {
                        transformedQuery.setParameters(query.getParameters());
                    }
                    effectiveQuery = transformedQuery;
                } catch (TemplateRenderException ex) {
                    throw new DBException(buildErrorMessage(ex), ex);
                }
            }

            if (delegate != null) {
                return delegate.transformQuery(dataSource, syntaxManager, effectiveQuery);
            }
            return effectiveQuery;
        }
    }
}
