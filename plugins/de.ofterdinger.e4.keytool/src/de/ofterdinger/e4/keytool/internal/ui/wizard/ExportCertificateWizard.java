package de.ofterdinger.e4.keytool.internal.ui.wizard;

import static de.ofterdinger.e4.keytool.internal.KeytoolPlugin.PLUGIN_ID;
import static org.eclipse.core.runtime.IStatus.ERROR;

import de.ofterdinger.e4.keytool.internal.KeytoolPlugin;
import de.ofterdinger.e4.keytool.internal.certificate.CertTools;
import de.ofterdinger.e4.keytool.internal.certificate.CompleteCertificate;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.cert.CertificateEncodingException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class ExportCertificateWizard extends Wizard implements INewWizard {
  private static final int NO_OF_TASKS = 2;
  private static final String TITLE = "Export the certificate"; // $NON-NLS-1$
  private CompleteCertificate completeCertificate;
  private ExportCertificatePage expPage;

  public ExportCertificateWizard(CompleteCertificate completeCertificate) {
    setWindowTitle(TITLE);
    this.completeCertificate = completeCertificate;
  }

  @Override
  public void addPages() {
    this.expPage = new ExportCertificatePage(TITLE, this.completeCertificate);
    addPage(this.expPage);
  }

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    // nothing to do yet
  }

  @Override
  public boolean performFinish() {
    final String filename = this.expPage.getFilename();
    final boolean exportPrivateKey = this.expPage.exportPrivateKey();
    if (exportPrivateKey) {
      try {
        this.completeCertificate =
            CertTools.getCompleteCertificate(
                this.completeCertificate.getKeystoreFile(),
                this.completeCertificate.getAlias(),
                this.expPage.getCertificatePassword());
      } catch (Exception e) {
        KeytoolPlugin.getDefault().getLog().log(new Status(ERROR, PLUGIN_ID, e.getMessage(), e));
        MessageDialog.openError(getShell(), "Error", e.getMessage()); // $NON-NLS-1$
      }
    }
    IRunnableWithProgress op =
        monitor -> {
          try {
            try {
              if (exportPrivateKey) {
                ExportCertificateWizard.this.createCertificateFileWithPrivateKey(
                    ExportCertificateWizard.this.completeCertificate.getAlias(), filename, monitor);
              } else {
                ExportCertificateWizard.this.createCertificateFile(
                    ExportCertificateWizard.this.completeCertificate.getAlias(), filename, monitor);
              }
            } catch (Exception e) {
              KeytoolPlugin.getDefault()
                  .getLog()
                  .log(new Status(ERROR, PLUGIN_ID, e.getMessage(), e));
              throw new InvocationTargetException(
                  new RuntimeException("Error creating certificate!")); // $NON-NLS-1$
            }
          } finally {
            monitor.done();
          }
        };
    try {
      getContainer().run(true, false, op);
    } catch (InterruptedException interruptedException) {
      Thread.currentThread().interrupt();
      return false;
    } catch (InvocationTargetException e) {
      KeytoolPlugin.getDefault().getLog().log(new Status(ERROR, PLUGIN_ID, e.getMessage(), e));
      Throwable realException = e.getTargetException();
      MessageDialog.openError(getShell(), "Error", realException.getMessage()); // $NON-NLS-1$
      return false;
    }
    return true;
  }

  private void createCertificateFile(String alias, String filename, IProgressMonitor monitor)
      throws CertificateEncodingException, IOException {
    monitor.beginTask("Saving " + alias + " certificate", NO_OF_TASKS); // $NON-NLS-1$ //$NON-NLS-2$
    try (FileOutputStream fos = new FileOutputStream(new File(filename))) {
      monitor.worked(1);
      fos.write(this.completeCertificate.getCertificate().getEncoded());
    } finally {
      monitor.worked(2);
    }
  }

  private void createCertificateFileWithPrivateKey(
      String alias, String filename, IProgressMonitor monitor) {
    monitor.beginTask("Saving " + alias + " certificate", NO_OF_TASKS); // $NON-NLS-1$ //$NON-NLS-2$
    monitor.worked(1);
    CertTools.exportPersonalCertToPFX(this.completeCertificate, filename);
    monitor.worked(2);
  }
}
