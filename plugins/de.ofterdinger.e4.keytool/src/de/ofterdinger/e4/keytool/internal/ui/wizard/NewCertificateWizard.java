package de.ofterdinger.e4.keytool.internal.ui.wizard;

import static de.ofterdinger.e4.keytool.internal.KeytoolPlugin.PLUGIN_ID;
import static org.eclipse.core.runtime.IStatus.ERROR;

import de.ofterdinger.e4.keytool.internal.KeystoreFile;
import de.ofterdinger.e4.keytool.internal.KeytoolPlugin;
import de.ofterdinger.e4.keytool.internal.certificate.CertTools;
import de.ofterdinger.e4.keytool.internal.certificate.CertificatePerson;
import de.ofterdinger.e4.keytool.internal.certificate.CompleteCertificate;
import de.ofterdinger.e4.keytool.internal.certificate.KeystoreType;
import de.ofterdinger.e4.keytool.internal.ui.KeyStoreView;
import de.ofterdinger.e4.keytool.internal.ui.editor.CertificateInput;
import de.ofterdinger.e4.keytool.internal.ui.util.TextConstants;
import de.ofterdinger.e4.keytool.internal.ui.util.TreeParent;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Date;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

public class NewCertificateWizard extends Wizard implements INewWizard {
  public static final String TITLE = "Create a new certificate";
  private static final int NO_OF_TASKS = 3;
  private NewCertificatePage newCertificatePage;
  private NewKeystorePage newKeystorePage;
  private TreeParent selection = null;
  private ShowCertificatePage showCertificatePage;

  public NewCertificateWizard() {
    setNeedsProgressMonitor(true);
    setWindowTitle(TITLE);
  }

  public NewCertificateWizard(TreeParent selection) {
    this.selection = selection;
  }

  @Override
  public final void addPages() {
    this.newCertificatePage = new NewCertificatePage(this.selection);
    addPage(this.newCertificatePage);
    this.newKeystorePage = new NewKeystorePage();
    addPage(this.newKeystorePage);
    this.showCertificatePage = new ShowCertificatePage("Verify information");
    addPage(this.showCertificatePage);
  }

  public final AbstractWizardPage getNewCertificatePage() {
    return this.newCertificatePage;
  }

  public final NewKeystorePage getNewKeystorePage() {
    return this.newKeystorePage;
  }

  public final ShowCertificatePage getShowCertificatePage() {
    return this.showCertificatePage;
  }

  @Override
  public void init(IWorkbench workbench, IStructuredSelection structSelection) {
    // nothing to do yet
  }

  @Override
  public final boolean performFinish() {
    final String alias = this.newCertificatePage.getAlias();
    final CertificatePerson person = this.newCertificatePage.getPerson();
    final String filename = this.newCertificatePage.getFilename();
    final String password = this.newCertificatePage.getPasswordText().getText();
    final String newFilename = this.newKeystorePage.getFilename();
    final String filenamePassword = this.newKeystorePage.getPassword();
    final KeystoreType keystoreType = this.newKeystorePage.getKeystoreType();
    final Date notBefore = this.newCertificatePage.getFromDate();
    final Date notAfter = this.newCertificatePage.getToDate();
    IRunnableWithProgress op =
        monitor -> {
          try {
            if (AbstractWizardPage.CREATE_A_NEW_KEYSTORE_TEXT.equals(filename)) {
              NewCertificateWizard.this.insertIntoNewKeystore(
                  alias,
                  filenamePassword,
                  newFilename,
                  password,
                  keystoreType,
                  person,
                  monitor,
                  notBefore,
                  notAfter);
            } else {
              NewCertificateWizard.this.insertIntoKeystore(
                  alias, password, filename, person, monitor, notBefore, notAfter);
              KeyStoreView.fireFileChanged(filename, keystoreType, password);
            }
          } finally {
            monitor.done();
          }
        };
    try {
      getContainer().run(true, false, op);
    } catch (InterruptedException interruptedException) {
      Thread.currentThread().interrupt();
      KeytoolPlugin.getDefault()
          .getLog()
          .warn(interruptedException.getMessage(), interruptedException);
      return false;
    } catch (InvocationTargetException e) {
      KeytoolPlugin.getDefault().getLog().log(new Status(ERROR, PLUGIN_ID, e.getMessage(), e));
      Throwable realException = e.getTargetException();
      MessageDialog.openError(getShell(), "Error", realException.getMessage());
      return false;
    }
    return true;
  }

  private void insertIntoKeystore(
      String alias,
      String password,
      String filename,
      CertificatePerson person,
      IProgressMonitor monitor,
      Date notBefore,
      Date notAfter) {
    monitor.beginTask("Creating " + alias + " certificate", 3);
    CompleteCertificate completeCertificate =
        CertTools.createCertificate(person, notBefore, notAfter);
    KeystoreFile keystoreFile = KeyStoreView.getKeystoreFile(filename);
    KeyStore keystore = keystoreFile.getKeystore();
    completeCertificate.setKeystoreFile(keystoreFile);
    monitor.worked(1);
    try (FileOutputStream fos = new FileOutputStream(keystoreFile.getKeystorefilename())) {
      keystore.setCertificateEntry(alias, completeCertificate.getCertificate());
      Certificate[] chain = new Certificate[] {completeCertificate.getCertificate()};
      keystore.setKeyEntry(
          alias, completeCertificate.getPrivateKey(), password.toCharArray(), chain);
      keystore.store(fos, keystoreFile.getPassword().toCharArray());
    } catch (Exception e) {
      KeytoolPlugin.showError(
          "Error inserting certificate into keystore!", getContainer().getShell());
      KeytoolPlugin.getDefault().getLog().log(new Status(ERROR, PLUGIN_ID, e.getMessage(), e));
    }
    openFileForEditing(alias, monitor, completeCertificate);
    monitor.worked(1);
  }

  private void insertIntoNewKeystore(
      String alias,
      String filePassword,
      String filename,
      String password,
      KeystoreType keystoreType,
      CertificatePerson person,
      IProgressMonitor monitor,
      Date notBefore,
      Date notAfter) {
    monitor.beginTask("Creating " + alias + " certificate", NO_OF_TASKS);
    CompleteCertificate completeCertificate =
        CertTools.createCertificate(person, notBefore, notAfter);
    completeCertificate.setAlias(alias);
    completeCertificate.setPassword(password);
    try {
      CertTools.addCertificateToNewKeystore(
          completeCertificate, alias, password, filePassword, filename, keystoreType);
      monitor.worked(1);
    } catch (Exception e) {
      KeytoolPlugin.showMessage("Error", "Error creating keystore!", getContainer().getShell());
      KeytoolPlugin.getDefault().getLog().log(new Status(ERROR, PLUGIN_ID, e.getMessage(), e));
    }
    KeyStoreView.addMonitorFile(filename, keystoreType, filePassword);
    completeCertificate.setKeystoreFile(KeyStoreView.getKeystoreFile(filename));
    openFileForEditing(alias, monitor, completeCertificate);
    monitor.worked(1);
  }

  private void openFileForEditing(
      String alias, IProgressMonitor monitor, final CompleteCertificate completeCertificate) {
    completeCertificate.setKeyEntry(true);
    completeCertificate.setAlias(alias);
    monitor.worked(1);
    monitor.setTaskName("Opening file for editing...");
    getShell()
        .getDisplay()
        .asyncExec(
            () -> {
              IWorkbenchPage page =
                  PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
              try {
                CertificateInput certificateInput = new CertificateInput(completeCertificate);
                IDE.openEditor(page, certificateInput, TextConstants.CERTIFICATE_EDITOR_ID, true);
              } catch (PartInitException e) {
                KeytoolPlugin.showError(
                    "Error showing certificate!",
                    NewCertificateWizard.this.getContainer().getShell());
                KeytoolPlugin.getDefault()
                    .getLog()
                    .log(new Status(ERROR, PLUGIN_ID, e.getMessage(), e));
              }
            });
  }
}
