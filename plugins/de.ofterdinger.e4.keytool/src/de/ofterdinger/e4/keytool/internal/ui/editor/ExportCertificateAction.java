package de.ofterdinger.e4.keytool.internal.ui.editor;

import de.ofterdinger.e4.keytool.internal.certificate.CompleteCertificate;
import de.ofterdinger.e4.keytool.internal.ui.util.ImageKeys;
import de.ofterdinger.e4.keytool.internal.ui.wizard.ExportCertificateWizard;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

public class ExportCertificateAction extends Action {
  private CompleteCertificate completeCertificate;
  private Shell shell;

  public ExportCertificateAction(CompleteCertificate completeCertificate) {
    setText("Export certificate"); // $NON-NLS-1$
    setToolTipText("Export the open certificate"); // $NON-NLS-1$
    setImageDescriptor(ImageKeys.getImageDescriptor(ImageKeys.EXPORT_CERTIFICATE));
    this.completeCertificate = completeCertificate;
  }

  public final CompleteCertificate getCompleteCertificate() {
    return this.completeCertificate;
  }

  @Override
  public final void run() {
    super.run();
    if (this.completeCertificate != null) {
      ExportCertificateWizard wizard = new ExportCertificateWizard(this.completeCertificate);
      WizardDialog dialog = new WizardDialog(this.shell, wizard);
      dialog.open();
    }
  }

  public final void setCompleteCertificate(CompleteCertificate completeCertificate) {
    this.completeCertificate = completeCertificate;
  }

  public final void setShell(Shell shell) {
    this.shell = shell;
  }
}
