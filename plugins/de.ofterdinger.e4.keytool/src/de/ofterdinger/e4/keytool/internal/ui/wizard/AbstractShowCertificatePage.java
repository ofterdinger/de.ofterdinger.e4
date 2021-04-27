package de.ofterdinger.e4.keytool.internal.ui.wizard;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import de.ofterdinger.e4.keytool.internal.certificate.CertificatePerson;

public abstract class AbstractShowCertificatePage extends AbstractWizardPage {
  private String alias = EMPTY;
  private Text aliasText;
  private Text countryText;
  private Text location;
  private Text orgText;
  private Text orgUnitText;
  private CertificatePerson person =
      new CertificatePerson(EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY);
  private Text personNameText;
  private boolean showAll = true;
  private Text stateText;
  private String validFrom = EMPTY;
  private Text validFromText;
  private String validTo = EMPTY;
  private Text validToText;

  protected AbstractShowCertificatePage(String pageName) {
    super(pageName);
  }

  public void setAlias(String alias) {
    this.alias = alias;
    if (this.aliasText != null) {
      this.aliasText.setText(alias);
    }
  }

  public void setPerson(CertificatePerson person) {
    if (person == null) {
      this.person = new CertificatePerson();
    } else {
      this.person = person;
    }
    if (this.personNameText != null) {
      this.personNameText.setText(this.person.getName());
      this.orgUnitText.setText(this.person.getOrganizationUnit());
      this.orgText.setText(this.person.getOrganization());
      this.location.setText(this.person.getLocation());
      this.stateText.setText(this.person.getState());
      this.countryText.setText(this.person.getCountryCode());
    }
  }

  public void setShowAll(boolean showAll) {
    this.showAll = showAll;
  }

  public void setValidFrom(String validFrom) {
    this.validFrom = validFrom;
    if (this.validFromText != null) {
      this.validFromText.setText(validFrom);
    }
  }

  public void setValidTo(String validTo) {
    this.validTo = validTo;
    if (this.validToText != null) {
      this.validToText.setText(validTo);
    }
  }

  protected void addCertificateToPage(Composite composite) {
    this.aliasText = this.makeLine(composite, "Alias", this.alias);
    if (this.showAll) {
      this.personNameText =
          this.makeLine(composite, "Your first and last name", this.person.getName());
      this.orgUnitText =
          this.makeLine(
              composite, "Name of your organizational unit", this.person.getOrganizationUnit());
      this.orgText =
          this.makeLine(composite, "Name of your organizational", this.person.getOrganization());
      this.location =
          this.makeLine(composite, "Name of your City or Locality", this.person.getLocation());
      this.stateText =
          this.makeLine(composite, "Name of your State or Province", this.person.getState());
      this.countryText =
          this.makeLine(
              composite, "The two-letter country code for this unit", this.person.getCountryCode());
    }
    this.validFromText =
        this.makeLine(composite, "Valid from (dd-mm-yyyy)", this.validFrom); 
    this.validToText =
        this.makeLine(composite, "Valid to (dd-mm-yyyy)", this.validTo); 
  }
}
