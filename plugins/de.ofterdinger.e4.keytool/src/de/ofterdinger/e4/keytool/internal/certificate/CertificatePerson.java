package de.ofterdinger.e4.keytool.internal.certificate;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class CertificatePerson {
	private static final int INITIAL_BUF_SIZE = 256;
	private String countryCode;
	private String email;
	private String location;
	private String name;
	private String organization;
	private String organizationUnit;
	private String state;

	public CertificatePerson() {
	}

	public CertificatePerson(String name, String organizationUnit, String organization, String city, String state, String countryCode) {
		this.name = name;
		this.organizationUnit = organizationUnit;
		this.organization = organization;
		this.location = city;
		this.state = state;
		this.countryCode = countryCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		CertificatePerson other = (CertificatePerson) obj;
		EqualsBuilder builder = new EqualsBuilder();
		builder.append(this.countryCode, other.countryCode);
		builder.append(this.email, other.email);
		builder.append(this.location, other.location);
		builder.append(this.name, other.name);
		builder.append(this.organization, other.organization);
		builder.append(this.organizationUnit, other.organizationUnit);
		builder.append(this.state, other.state);
		return builder.isEquals();
	}

	public String getCountryCode() {
		return this.countryCode;
	}

	public String getEmail() {
		return this.email;
	}

	public String getLocation() {
		return this.location;
	}

	public String getName() {
		return this.name;
	}

	public String getOrganization() {
		return this.organization;
	}

	public String getOrganizationUnit() {
		return this.organizationUnit;
	}

	public String getState() {
		return this.state;
	}

	@Override
	public int hashCode() {
		int result = 1;
		result = 31 * result + (this.countryCode == null ? 0 : this.countryCode.hashCode());
		result = 31 * result + (this.email == null ? 0 : this.email.hashCode());
		result = 31 * result + (this.location == null ? 0 : this.location.hashCode());
		result = 31 * result + (this.name == null ? 0 : this.name.hashCode());
		result = 31 * result + (this.organization == null ? 0 : this.organization.hashCode());
		result = 31 * result + (this.organizationUnit == null ? 0 : this.organizationUnit.hashCode());
		result = 31 * result + (this.state == null ? 0 : this.state.hashCode());
		return result;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public void setOrganizationUnit(String organizationUnit) {
		this.organizationUnit = organizationUnit;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Override
	public String toString() {
		StringBuilder buff = new StringBuilder(INITIAL_BUF_SIZE);
		append("CN=", this.name, buff); //$NON-NLS-1$
		append("OU=", this.organizationUnit, buff); //$NON-NLS-1$
		append("O=", this.organization, buff); //$NON-NLS-1$
		append("L=", this.location, buff); //$NON-NLS-1$
		append("ST=", this.state, buff); //$NON-NLS-1$
		append("C=", this.countryCode, buff); //$NON-NLS-1$
		return buff.toString();
	}

	private static void append(String leadingText, String value, StringBuilder buff) {
		if (buff.length() > 0) {
			buff.append(", "); //$NON-NLS-1$
		}
		if (value != null && value.length() > 0) {
			buff.append(leadingText).append(value);
		} else {
			buff.append(leadingText).append("Unknown"); //$NON-NLS-1$
		}
	}
}
