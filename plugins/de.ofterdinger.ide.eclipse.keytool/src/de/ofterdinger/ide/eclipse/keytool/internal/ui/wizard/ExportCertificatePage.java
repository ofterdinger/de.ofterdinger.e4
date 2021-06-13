package de.ofterdinger.ide.eclipse.keytool.internal.ui.wizard;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import de.ofterdinger.ide.eclipse.keytool.internal.KeytoolPlugin;
import de.ofterdinger.ide.eclipse.keytool.internal.certificate.CertificatePerson;
import de.ofterdinger.ide.eclipse.keytool.internal.certificate.CompleteCertificate;
import java.io.File;
import java.security.cert.X509Certificate;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ExportCertificatePage extends AbstractShowCertificatePage {
  private Text certificatePasswordText;
  private Button exportPrivateKeyCheckBox;
  private final CompleteCertificate completeCertificate;
  private String extension;
  private Text filenameText;

  public ExportCertificatePage(String pageName, CompleteCertificate completeCertificate) {
    super(pageName);
    setTitle("Export the certificate");
    setDescription(
        "Export the open certificate and put it in a file. Usually its postfix should be 'cer'.");
    if (completeCertificate.getCertificatePerson() == null) {
      CertificatePerson certificatePerson = new CertificatePerson();
      if (completeCertificate.getCertificate() instanceof X509Certificate) {
        X509Certificate x509 = (X509Certificate) completeCertificate.getCertificate();
        certificatePerson.setName(x509.getSubjectX500Principal().getName());
      }
      completeCertificate.setCertificatePerson(certificatePerson);
    }
    if (completeCertificate.getCertificate() instanceof X509Certificate) {
      X509Certificate x509 = (X509Certificate) completeCertificate.getCertificate();
      setValidFrom(x509.getNotBefore().toString());
      setValidTo(x509.getNotAfter().toString());
    }
    setShowAll(false);
    setAlias(completeCertificate.getAlias());
    setPerson(completeCertificate.getCertificatePerson());
    this.completeCertificate = completeCertificate;
    setPageComplete(false);
  }

  @Override
  public final void createControl(Composite parent) {
    Composite composite = new Composite(parent, 0);
    GridLayout layout = new GridLayout(3, false);
    setColspan(2);
    composite.setLayout(layout);
    setEditable(false);
    if (this.completeCertificate.isKeyEntry()) {
      this.exportPrivateKeyCheckBox = new Button(composite, SWT.CHECK);
      this.exportPrivateKeyCheckBox.setText("Export private key");
      GridData gd = new GridData();
      gd.horizontalSpan = 3;
      this.exportPrivateKeyCheckBox.setLayoutData(gd);
      this.exportPrivateKeyCheckBox.addSelectionListener(
          new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
              if (ExportCertificatePage.this.exportPrivateKeyCheckBox.getSelection()) {
                ExportCertificatePage.this.certificatePasswordText.setEditable(true);
                ExportCertificatePage.this.certificatePasswordText.setEnabled(true);
              } else {
                ExportCertificatePage.this.certificatePasswordText.setEditable(false);
                ExportCertificatePage.this.certificatePasswordText.setEnabled(false);
              }
              ExportCertificatePage.this.dialogChanged();
            }
          });
    }
    Label filenameLabel = new Label(composite, 0);
    filenameLabel.setText("&Filename:");
    filenameLabel.setLayoutData(new GridData(4, 2, false, false));
    this.filenameText = new Text(composite, 2048);
    this.filenameText.setLayoutData(new GridData(4, 2, true, false));
    this.filenameText.addModifyListener(getModifyListener());
    addBrowseButton(composite, this.filenameText);
    if (this.completeCertificate.isKeyEntry()) {
      this.certificatePasswordText = this.makeLine(composite, "Certificate&password:", EMPTY);
    }
    addCertificateToPage(composite);
    setControl(composite);
  }

  public boolean exportPrivateKey() {
    if (this.exportPrivateKeyCheckBox == null) {
      return false;
    }
    return this.exportPrivateKeyCheckBox.getSelection();
  }

  public String getCertificatePassword() {
    return this.certificatePasswordText.getText();
  }

  public String getFilename() {
    return this.filenameText.getText();
  }

  @Override
  public boolean isPageComplete() {
    return (!(this.filenameText.getText().length() <= 0
        || exportPrivateKey() && this.certificatePasswordText.getText().length() <= 0));
  }

  @Override
  protected void dialogChanged() {
    updateStatus(null);
    this.checkStatus(this.filenameText, "Filename must be specified");
  }

  @Override
  protected String[] getExtensions() {
    this.extension = ".cer";
    if (exportPrivateKey()) {
      this.extension = ".pfx";
    }
    return new String[] {"*" + this.extension};
  }

  @Override
  protected void setFilenameText(Text filenameText, String file) {
    if (!file.endsWith(this.extension)) {
      file = String.valueOf(file) + this.extension;
    }
    filenameText.setText(file);
    if (new File(file).isFile()) {
      KeytoolPlugin.showMessage(
          "File exists", "File already exists and will be overriden", getContainer().getShell());
    }
  }
}
