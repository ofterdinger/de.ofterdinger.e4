package de.ofterdinger.e4.keytool.internal.ui.action;

import static de.ofterdinger.e4.keytool.internal.KeytoolPlugin.PLUGIN_ID;
import static org.eclipse.core.runtime.IStatus.ERROR;

import de.ofterdinger.e4.keytool.internal.KeytoolPlugin;
import de.ofterdinger.e4.keytool.internal.certificate.CertTools;
import de.ofterdinger.e4.keytool.internal.certificate.CompleteCertificate;
import de.ofterdinger.e4.keytool.internal.ui.util.ImageKeys;
import de.ofterdinger.e4.keytool.internal.ui.util.TreeObject;
import de.ofterdinger.e4.keytool.internal.ui.wizard.ExportCertificateWizard;
import java.security.KeyStoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

public class ExportCertificateFromViewAction extends AbstractKeytoolViewAction {
  private Shell shell;

  public ExportCertificateFromViewAction(IWorkbenchWindow window) {
    super(window);
    this.shell = window.getShell();
  }

  public Shell getShell() {
    return this.shell;
  }

  @Override
  public void run() {
    super.run();
    TreeObject selectedTreeObject = getSelectedTreeObject();
    if (selectedTreeObject != null && selectedTreeObject.hasParentKeystore()) {
      try {
        CompleteCertificate completeCertificate =
            CertTools.getCertificate(
                selectedTreeObject.getParent().getKeystoreFile(), selectedTreeObject.getName());
        if (completeCertificate != null) {
          ExportCertificateWizard wizard = new ExportCertificateWizard(completeCertificate);
          WizardDialog dialog = new WizardDialog(this.shell, wizard);
          dialog.open();
        }
      } catch (KeyStoreException e) {
        KeytoolPlugin.getDefault().getLog().log(new Status(ERROR, PLUGIN_ID, e.getMessage(), e));
      }
    }
  }

  @Override
  protected void actionFiredChild() {
    setEnabled(true);
  }

  @Override
  protected void actionFiredParent() {
    setEnabled(false);
  }

  @Override
  protected ImageDescriptor getActionImage() {
    return ImageKeys.getImageDescriptor(ImageKeys.EXPORT_CERTIFICATE);
  }

  @Override
  protected String getActionText() {
    return "Export certificate"; //$NON-NLS-1$
  }

  @Override
  protected String getActionToolTipText() {
    return "Export the open certificate"; //$NON-NLS-1$
  }
}
