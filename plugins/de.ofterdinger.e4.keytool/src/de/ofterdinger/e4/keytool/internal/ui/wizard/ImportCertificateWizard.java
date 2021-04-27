package de.ofterdinger.e4.keytool.internal.ui.wizard;

import static de.ofterdinger.e4.keytool.internal.KeytoolPlugin.PLUGIN_ID;
import static org.eclipse.core.runtime.IStatus.ERROR;

import de.ofterdinger.e4.keytool.internal.KeystoreFile;
import de.ofterdinger.e4.keytool.internal.KeytoolPlugin;
import de.ofterdinger.e4.keytool.internal.certificate.CertTools;
import de.ofterdinger.e4.keytool.internal.certificate.CompleteCertificate;
import de.ofterdinger.e4.keytool.internal.certificate.KeystoreType;
import de.ofterdinger.e4.keytool.internal.ui.KeyStoreView;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class ImportCertificateWizard extends Wizard implements INewWizard {
  private static final String TITLE = "Import a certificate"; // $NON-NLS-1$
  private File fileToImport = null;
  private ImportCertificatePage impPage;
  private NewKeystorePage newKeystorePage;

  public ImportCertificateWizard() {
    setWindowTitle(TITLE);
    setNeedsProgressMonitor(true);
  }

  @Override
  public void addPages() {
    super.addPages();
    this.impPage = new ImportCertificatePage("Choose the certificatefile"); // $NON-NLS-1$
    if (this.fileToImport != null) {
      this.impPage.setFileToImport(this.fileToImport);
    }
    addPage(this.impPage);
    this.newKeystorePage = new NewKeystorePage();
    addPage(this.newKeystorePage);
  }

  @Override
  public boolean canFinish() {
    return (this.impPage.isPageComplete()
        && (!this.impPage.isNewKeystore()
            || this.impPage.isNewKeystore()
                && this.newKeystorePage.getFilename().length() > 0
                && this.newKeystorePage.getPassword().length() > 0));
  }

  public NewKeystorePage getNewKeystorePage() {
    return this.newKeystorePage;
  }

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    // nothing to do yet
  }

  @Override
  public boolean performFinish() {
    final String alias = this.impPage.getAliasText();
    final String filename = this.impPage.getFilename();
    final String password = this.impPage.getPassword();
    final boolean isWithPrivateKey = this.impPage.isWithPrivateKey();
    final boolean isNewKeystore = this.impPage.isNewKeystore();
    final String keystoreFilename =
        isNewKeystore ? this.newKeystorePage.getFilename() : this.impPage.getKeystoreFilename();
    final String keystorePassword = this.newKeystorePage.getPassword();
    final KeystoreType keystoreType = this.newKeystorePage.getKeystoreType();
    IRunnableWithProgress op =
        new IRunnableWithProgress() {

          @Override
          public void run(IProgressMonitor monitor) throws InvocationTargetException {
            try {
              try {
                importCertificateFile(monitor);
              } catch (Exception e) {
                KeytoolPlugin.getDefault()
                    .getLog()
                    .log(new Status(ERROR, PLUGIN_ID, e.getMessage(), e));
                throw new InvocationTargetException(e);
              }
            } finally {
              monitor.done();
            }
          }

          private void importCertificateFile(IProgressMonitor monitor)
              throws UnrecoverableKeyException, KeyStoreException, NoSuchProviderException,
                  NoSuchAlgorithmException, CertificateException, InvalidKeySpecException,
                  IOException {
            CompleteCertificate certificate;
            monitor.beginTask("Importing certificate", 4); // $NON-NLS-1$
            monitor.worked(1);
            if (isWithPrivateKey) {
              certificate = CertTools.loadPFX(filename, password);
            } else {
              certificate = new CompleteCertificate();
              certificate.setCertificate(CertTools.loadCertificate(filename));
            }
            certificate.setAlias(alias);
            certificate.setPassword(password);
            monitor.worked(1);
            if (isNewKeystore) {
              insertIntoNewKeystore(certificate, monitor);
            } else {
              KeystoreFile keystoreFile = KeyStoreView.getKeystoreFile(keystoreFilename);
              CertTools.addCertificateAndSaveKeystore(certificate, keystoreFile);
              KeyStoreView.fireFileChanged(keystoreFilename, keystoreType, keystorePassword);
            }
            monitor.worked(1);
          }

          private void insertIntoNewKeystore(
              CompleteCertificate completeCertificate, IProgressMonitor monitor)
              throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
                  IOException {
            CertTools.addCertificateToNewKeystore(
                completeCertificate, alias, password, keystorePassword, filename, keystoreType);
            monitor.worked(1);
            KeyStoreView.addMonitorFile(filename, keystoreType, keystorePassword);
            monitor.worked(1);
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

  public void setFileToImport(File file) {
    this.fileToImport = file;
  }
}
