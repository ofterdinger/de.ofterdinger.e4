package de.ofterdinger.ide.eclipse.keytool.internal.ui.handler;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;

import de.ofterdinger.ide.eclipse.keytool.internal.ui.wizard.NewCertificateWizard;

public class NewCertificateHandler extends AbstractKeytoolHandler {

  @Override
  public void execute(IWorkbenchWindow window) throws ExecutionException {
    NewCertificateWizard wizard = new NewCertificateWizard();
    WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
    dialog.create();
    dialog.open();
  }
}
