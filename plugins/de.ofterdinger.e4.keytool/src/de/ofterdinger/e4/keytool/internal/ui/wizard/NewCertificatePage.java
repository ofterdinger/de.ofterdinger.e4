package de.ofterdinger.e4.keytool.internal.ui.wizard;

import de.ofterdinger.e4.keytool.internal.certificate.CertificatePerson;
import de.ofterdinger.e4.keytool.internal.ui.util.TreeParent;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class NewCertificatePage extends AbstractWizardPage {
  private static final int DEFAULT_YEAR_TO_ADD = 5;
  private static final String LABEL_ALIAS = "Alias";
  private static final String LABEL_NAME_OF_YOUR_CITY_OR_LOCALITY = "Name of your City or Locality";
  private static final String LABEL_NAME_OF_YOUR_ORGANIZATIONAL = "Name of your organizational";
  private static final String LABEL_NAME_OF_YOUR_ORGANIZATIONAL_UNIT =
      "Name of your organizational unit";
  private static final String LABEL_NAME_OF_YOUR_STATE_OR_PROVINCE =
      "Name of your State or Province";
  private static final String LABEL_PASS = "Password";
  private static final String LABEL_THE_TWO_LETTER_COUNTRY_CODE_FOR_THIS_UNIT =
      "The two-letter country code for this unit";
  private static final String LABEL_VALID_FROM_DD_MM_YYYY = "Valid from (dd-mm-yyyy)";
  private static final String LABEL_VALID_TO_DD_MM_YYYY = "Valid to (dd-mm-yyyy)";
  private static final String LABEL_YOUR_FIRST_AND_LAST_NAME = "Your first and last name";
  private static final int NO_OF_COLUMNS = 2;
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
    layout.numColumns = NO_OF_COLUMNS;
    this.keystoreCombo = addOpenKeystores(container, this.selection);
    this.keystoreCombo.addDisposeListener(e -> this.keystoreCombo = null);
    this.aliasText = this.makeLine(container, LABEL_ALIAS, StringUtils.EMPTY);
    this.passwordText = this.makeLine(container, LABEL_PASS, StringUtils.EMPTY);
    this.firstAndLastnameText =
        this.makeLine(container, LABEL_YOUR_FIRST_AND_LAST_NAME, StringUtils.EMPTY);
    this.orgUnitText =
        this.makeLine(container, LABEL_NAME_OF_YOUR_ORGANIZATIONAL_UNIT, StringUtils.EMPTY);
    this.orgText = this.makeLine(container, LABEL_NAME_OF_YOUR_ORGANIZATIONAL, StringUtils.EMPTY);
    this.cityText =
        this.makeLine(container, LABEL_NAME_OF_YOUR_CITY_OR_LOCALITY, StringUtils.EMPTY);
    this.stateText =
        this.makeLine(container, LABEL_NAME_OF_YOUR_STATE_OR_PROVINCE, StringUtils.EMPTY);
    Locale defaultLocale = Locale.getDefault();
    this.countryText =
        this.makeLine(
            container, LABEL_THE_TWO_LETTER_COUNTRY_CODE_FOR_THIS_UNIT, defaultLocale.getCountry());
    this.fromDateText =
        this.makeLine(container, LABEL_VALID_FROM_DD_MM_YYYY, getDateFormat().format(new Date()));
    Calendar c = Calendar.getInstance();
    c.add(1, DEFAULT_YEAR_TO_ADD);
    this.toDateText =
        this.makeLine(container, LABEL_VALID_TO_DD_MM_YYYY, getDateFormat().format(c.getTime()));
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
