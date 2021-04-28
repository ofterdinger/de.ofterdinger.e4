package de.ofterdinger.e4.keytool.internal.ui.wizard;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import de.ofterdinger.e4.keytool.internal.certificate.KeystoreType;
import de.ofterdinger.e4.keytool.internal.ui.util.KeystoreUIHelper;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NewKeystorePage extends AbstractWizardPage {
  private Text filenameText;
  private Combo keystoreTypeCombo;
  private Text passwordText;

  public NewKeystorePage() {
    this("Select keystore to create");
  }

  public NewKeystorePage(String pageName) {
    super(pageName);
    setPageComplete(false);
    setTitle("Create a new certificate");
    setDescription(pageName);
  }

  @Override
  public void createControl(Composite parent) {
    Composite container = new Composite(parent, 0);
    GridLayout layout = new GridLayout();
    container.setLayout(layout);
    layout.numColumns = 3;
    addFilenameInputs(container);
    this.passwordText = this.makeLine(container, "Keystore password", EMPTY);
    new org.eclipse.swt.widgets.Label(container, 0);
    new Label(container, 0).setText("Keystore type");
    this.keystoreTypeCombo = new Combo(container, 12);
    KeystoreUIHelper.populateAvailableKeystoreTypes(this.keystoreTypeCombo);
    setControl(container);
  }

  public String getFilename() {
    return this.filenameText.getText();
  }

  public KeystoreType getKeystoreType() {
    return KeystoreType.getInstance(this.keystoreTypeCombo.getText());
  }

  @Override
  public IWizardPage getNextPage() {
    IWizard theWizard = getWizard();
    if (theWizard instanceof NewCertificateWizard) {
      ShowCertificatePage showCertificatePage =
          ((NewCertificateWizard) theWizard).getShowCertificatePage();
      showCertificatePage.setKeystore(this.filenameText.getText());
      return showCertificatePage;
    }
    return null;
  }

  String getPassword() {
    return this.passwordText.getText();
  }

  private void handleBrowse() {
    FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
    dialog.setText("Create keystore");
    String file = dialog.open();
    dialogChanged();
    if (StringUtils.isNotBlank(file)) {
      this.filenameText.setText(file);
    }
  }

  @Override
  protected void dialogChanged() {
    if (this.filenameText != null) {
      this.checkStatus(this.filenameText, "Filename must be specified.");
      if (this.filenameText.getText().length() > 0) {
        updateStatus(null);
      }
      this.checkStatus(this.passwordText, "Password for the keystore must be specified.");
    }
  }

  private void addFilenameInputs(Composite container) {
    Label label = new Label(container, 0);
    label.setText("Filename");

    GridData gridData = new GridData(4, 2, true, false);

    this.filenameText = new Text(container, 2048);
    this.filenameText.setLayoutData(gridData);
    this.filenameText.setText(EMPTY);
    this.filenameText.setEditable(true);
    this.filenameText.addModifyListener(getModifyListener());

    Button button = new Button(container, SWT.PUSH);
    button.setText("Browse...");
    button.addSelectionListener(
        SelectionListener.widgetSelectedAdapter(event -> NewKeystorePage.this.handleBrowse()));
  }
}
