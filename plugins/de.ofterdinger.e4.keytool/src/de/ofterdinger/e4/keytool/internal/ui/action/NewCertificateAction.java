package de.ofterdinger.e4.keytool.internal.ui.action;

import static de.ofterdinger.e4.keytool.internal.ui.util.ImageKeys.NEW_CERTIFICATE;

import de.ofterdinger.e4.keytool.internal.ui.util.ImageKeys;
import de.ofterdinger.e4.keytool.internal.ui.wizard.NewCertificateWizard;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

public class NewCertificateAction extends Action {
  private final Shell shell;

  public NewCertificateAction(Shell shell) {
    setText("New certificate"); // $NON-NLS-1$
    setToolTipText("Create a new certificate"); // $NON-NLS-1$
    setImageDescriptor(ImageKeys.getImageDescriptor(NEW_CERTIFICATE));
    this.shell = shell;
  }

  @Override
  public final void run() {
    NewCertificateWizard wizard = new NewCertificateWizard();
    WizardDialog dialog = new WizardDialog(this.shell, wizard);
    dialog.open();
  }
}
