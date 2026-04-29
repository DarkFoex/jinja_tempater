package com.example.dbeaver.jinja.ui;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public final class TemplateVariableDialog extends TitleAreaDialog {
    private static final int LOAD_EXAMPLE_BUTTON_ID = IDialogConstants.CLIENT_ID + 1;
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

    private Text jsonText;
    private Button saveLastVariablesButton;
    private String variablesJson;
    private boolean saveLastVariables;

    public TemplateVariableDialog(Shell parentShell, String initialJson, boolean saveLastVariables) {
        super(parentShell);
        this.variablesJson = initialJson == null ? "{}" : initialJson;
        this.saveLastVariables = saveLastVariables;
    }

    @Override
    public void create() {
        super.create();
        setTitle("Render Jinja Template");
        setMessage("Enter template variables as JSON.");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        container.setLayout(new GridLayout(1, false));

        jsonText = new Text(container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        GridData textData = new GridData(SWT.FILL, SWT.FILL, true, true);
        textData.widthHint = 720;
        textData.heightHint = 320;
        jsonText.setLayoutData(textData);
        jsonText.setText(variablesJson);

        saveLastVariablesButton = new Button(container, SWT.CHECK);
        saveLastVariablesButton.setText("Save last variables");
        saveLastVariablesButton.setSelection(saveLastVariables);

        return area;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, LOAD_EXAMPLE_BUTTON_ID, "Load example", false);
        createButton(parent, IDialogConstants.OK_ID, "Render", true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == LOAD_EXAMPLE_BUTTON_ID) {
            jsonText.setText(EXAMPLE_JSON);
            return;
        }
        super.buttonPressed(buttonId);
    }

    @Override
    protected void okPressed() {
        variablesJson = jsonText.getText();
        saveLastVariables = saveLastVariablesButton.getSelection();
        super.okPressed();
    }

    public String getVariablesJson() {
        return variablesJson;
    }

    public boolean isSaveLastVariables() {
        return saveLastVariables;
    }
}

