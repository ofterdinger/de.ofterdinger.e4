package de.ofterdinger.e4.keytool.internal.ui.wizard;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class ShowCertificatePage extends AbstractShowCertificatePage {
  private String keystore = EMPTY;
  private Text keystoreText;

  public ShowCertificatePage(String pageName) {
    super(pageName);
    setTitle("Create a new certificate"); 
    setDescription("Verify information"); 
  }

  @Override
  public void createControl(Composite parent) {
    Composite composite = new Composite(parent, 0);
    GridLayout layout = new GridLayout(2, false);
    composite.setLayout(layout);
    setEditable(false);
    this.keystoreText = this.makeLine(composite, "Keystore", this.keystore); 
    addCertificateToPage(composite);
    setControl(composite);
  }

  public String getKeystore() {
    return this.keystore;
  }

  @Override
  public boolean isPageComplete() {
    return true;
  }

  public void setKeystore(String keystore) {
    this.keystore = keystore;
    this.keystoreText.setText(keystore);
  }

  @Override
  protected void dialogChanged() {
    // nothing to do yet
  }
}
