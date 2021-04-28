package de.ofterdinger.e4.keytool.internal.ui.wizard;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import de.ofterdinger.e4.keytool.internal.certificate.CertificatePerson;
import de.ofterdinger.e4.keytool.internal.ui.util.TreeParent;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class NewCertificatePage extends AbstractWizardPage {
  private static final int DEFAULT_YEAR_TO_ADD = 5;
  private Text aliasText;
  private Text cityText;
  private Text countryText;
  private Text firstAndLastnameText;
  private Text fromDateText;
  private Combo keystoreCombo;
  private Text orgText;
  private Text orgUnitText;
  private Text passwordText;
  private TreeParent selection;
  private Text stateText;
  private Text toDateText;

  protected NewCertificatePage(String pageName) {
    super(pageName);
    setTitle("Create a new certificate");
    setDescription("This wizard creates a new certificate.");
    setPageComplete(false);
  }

  protected NewCertificatePage(TreeParent selection) {
    this("Create a new certificate");
    this.selection = selection;
  }

  @Override
  public void createControl(Composite parent) {
    Composite container = new Composite(parent, 0);
    GridLayout layout = new GridLayout();
    container.setLayout(layout);
    layout.numColumns = 2;
    this.keystoreCombo = addOpenKeystores(container, this.selection);
    this.keystoreCombo.addDisposeListener(e -> this.keystoreCombo = null);
    this.aliasText = this.makeLine(container, "Alias", EMPTY);
    this.passwordText = this.makeLine(container, "Password", EMPTY);
    this.firstAndLastnameText = this.makeLine(container, "Your first and last name", EMPTY);
    this.orgUnitText = this.makeLine(container, "Name of your organizational unit", EMPTY);
    this.orgText = this.makeLine(container, "Name of your organizational", EMPTY);
    this.cityText = this.makeLine(container, "Name of your City or Locality", EMPTY);
    this.stateText = this.makeLine(container, "Name of your State or Province", EMPTY);
    Locale defaultLocale = Locale.getDefault();
    this.countryText =
        this.makeLine(
            container, "The two-letter country code for this unit", defaultLocale.getCountry());
    this.fromDateText =
        this.makeLine(container, "Valid from (dd-mm-yyyy)", getDateFormat().format(new Date()));
    Calendar c = Calendar.getInstance();
    c.add(1, DEFAULT_YEAR_TO_ADD);
    this.toDateText =
        this.makeLine(container, "Valid to (dd-mm-yyyy)", getDateFormat().format(c.getTime()));
    setControl(container);
    resetStatus();
  }

  public String getAlias() {
    return this.aliasText.getText();
  }

  public String getFilename() {
    return this.keystoreCombo == null ? null : this.keystoreCombo.getText();
  }

  public Date getFromDate() {
    return parseDate(this.fromDateText);
  }

  @Override
  public IWizardPage getNextPage() {
    ShowCertificatePage showCertificatePage = getShowCertificatePage();
    showCertificatePage.setAlias(this.aliasText.getText());
    showCertificatePage.setPerson(getPerson());
    showCertificatePage.setValidFrom(this.fromDateText.getText());
    showCertificatePage.setValidTo(this.toDateText.getText());
    showCertificatePage.setKeystore(this.keystoreCombo.getText());
    if (AbstractWizardPage.CREATE_A_NEW_KEYSTORE_TEXT.equals(getFilename())) {
      return getNewKeystorePage();
    }
    return showCertificatePage;
  }

  public Text getPasswordText() {
    return this.passwordText;
  }

  public CertificatePerson getPerson() {
    return new CertificatePerson(
        this.firstAndLastnameText.getText(),
        this.orgUnitText.getText(),
        this.orgText.getText(),
        this.cityText.getText(),
        this.stateText.getText(),
        this.countryText.getText());
  }

  public Date getToDate() {
    return parseDate(this.toDateText);
  }

  @Override
  protected void dialogChanged() {
    NewKeystorePage newKeystorePage = getNewKeystorePage();
    boolean pageComplete = !AbstractWizardPage.CREATE_A_NEW_KEYSTORE_TEXT.equals(getFilename());
    newKeystorePage.setPageComplete(pageComplete);
    updateStatus(null);
    checkDate(this.toDateText, "Valid to");
    checkDate(this.fromDateText, "Valid from");
    this.checkStatus(this.firstAndLastnameText, "Firstname and lastname must be specified");
    this.checkStatus(this.passwordText, "Password must be specified");
    this.checkStatus(this.aliasText, "Alias must be specified");
    this.checkStatus(this.keystoreCombo, "Specify which keystore to save the certificate in");
  }

  private NewKeystorePage getNewKeystorePage() {
    return ((NewCertificateWizard) getWizard()).getNewKeystorePage();
  }

  private ShowCertificatePage getShowCertificatePage() {
    return ((NewCertificateWizard) getWizard()).getShowCertificatePage();
  }
}
