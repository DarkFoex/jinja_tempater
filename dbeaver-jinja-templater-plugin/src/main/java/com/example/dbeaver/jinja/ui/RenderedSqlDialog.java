package com.example.dbeaver.jinja.ui;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public final class RenderedSqlDialog extends TitleAreaDialog {
    private static final int COPY_BUTTON_ID = IDialogConstants.CLIENT_ID + 2;

    private final String renderedSql;
    private Text sqlText;

    public RenderedSqlDialog(Shell parentShell, String renderedSql) {
        super(parentShell);
        this.renderedSql = renderedSql == null ? "" : renderedSql;
    }

    @Override
    public void create() {
        super.create();
        setTitle("Rendered SQL");
        setMessage("Template rendered successfully. You can review and copy the SQL below.");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        container.setLayout(new GridLayout(1, false));

        sqlText = new Text(container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
        GridData textData = new GridData(SWT.FILL, SWT.FILL, true, true);
        textData.widthHint = 800;
        textData.heightHint = 420;
        sqlText.setLayoutData(textData);
        sqlText.setText(renderedSql);

        return area;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, COPY_BUTTON_ID, "Copy to Clipboard", false);
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == COPY_BUTTON_ID) {
            copyToClipboard();
            return;
        }
        super.buttonPressed(buttonId);
    }

    private void copyToClipboard() {
        Clipboard clipboard = new Clipboard(getShell().getDisplay());
        try {
            clipboard.setContents(new Object[] { renderedSql }, new Transfer[] { TextTransfer.getInstance() });
        } finally {
            clipboard.dispose();
        }
    }
}

