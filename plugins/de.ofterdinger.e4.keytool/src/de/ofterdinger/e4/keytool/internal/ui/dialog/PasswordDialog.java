package de.ofterdinger.e4.keytool.internal.ui.dialog;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Shell;

public class PasswordDialog extends InputDialog {
	public PasswordDialog(Shell parentShell, String dialogTitle, String dialogMessage, String initialValue, IInputValidator validator) {
		super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
	}

	@Override
	public int open() {
		if (getText() == null) {
			create();
		}
		getText().setEchoChar('*');
		return super.open();
	}
}
