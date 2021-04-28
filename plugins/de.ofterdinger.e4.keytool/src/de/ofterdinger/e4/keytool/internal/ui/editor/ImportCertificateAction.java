package de.ofterdinger.e4.keytool.internal.ui.editor;

import static de.ofterdinger.e4.keytool.internal.ui.util.ImageKeys.IMPORT_CERTIFICATE;

import de.ofterdinger.e4.keytool.internal.ui.util.ImageKeys;
import de.ofterdinger.e4.keytool.internal.ui.wizard.ImportCertificateWizard;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

public class ImportCertificateAction extends Action {
  private Shell shell;

  public ImportCertificateAction(IWorkbenchWindow window) {
    this.shell = window.getShell();
    setText("Import certificate");
    setToolTipText("Import an certificate into a keystore");
    setImageDescriptor(ImageKeys.getImageDescriptor(IMPORT_CERTIFICATE));
  }

  @Override
  public void run() {
    ImportCertificateWizard wizard = new ImportCertificateWizard();
    WizardDialog dialog = new WizardDialog(this.shell, wizard);
    dialog.open();
  }
}
