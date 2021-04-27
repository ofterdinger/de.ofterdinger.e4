package de.ofterdinger.e4.keytool.internal.ui.editor;

import static de.ofterdinger.e4.keytool.internal.ui.util.ImageKeys.IMPORT_CERTIFICATE;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

import de.ofterdinger.e4.keytool.internal.ui.util.ImageKeys;
import de.ofterdinger.e4.keytool.internal.ui.wizard.ImportCertificateWizard;

public class ImportCertificateAction extends Action {
	private Shell shell;

	public ImportCertificateAction(IWorkbenchWindow window) {
		this.shell = window.getShell();
		setText("Import certificate"); //$NON-NLS-1$
		setToolTipText("Import an certificate into a keystore"); //$NON-NLS-1$
		setImageDescriptor(ImageKeys.getImageDescriptor(IMPORT_CERTIFICATE));
	}

	@Override
	public void run() {
		ImportCertificateWizard wizard = new ImportCertificateWizard();
		WizardDialog dialog = new WizardDialog(this.shell, wizard);
		dialog.open();
	}
}
