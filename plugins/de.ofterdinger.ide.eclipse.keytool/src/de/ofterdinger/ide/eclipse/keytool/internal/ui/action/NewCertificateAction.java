package de.ofterdinger.ide.eclipse.keytool.internal.ui.action;

import static de.ofterdinger.ide.eclipse.keytool.internal.ui.util.ImageKeys.NEW_CERTIFICATE;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

import de.ofterdinger.ide.eclipse.keytool.internal.ui.util.ImageKeys;
import de.ofterdinger.ide.eclipse.keytool.internal.ui.wizard.NewCertificateWizard;

public class NewCertificateAction extends Action {
  private final Shell shell;

  public NewCertificateAction(Shell shell) {
    setText("New certificate");
    setToolTipText("Create a new certificate");
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
