package de.ofterdinger.e4.keytool.internal.certificate;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import org.apache.commons.lang3.builder.EqualsBuilder;

import de.ofterdinger.e4.keytool.internal.KeystoreFile;

public class CompleteCertificate {
	private String alias;
	private Certificate certificate;
	private CertificatePerson certificatePerson;
	private CompleteCertificate issuer;
	private boolean keyEntry = false;
	private KeystoreFile keystoreFile;
	private String password = EMPTY;
	private PrivateKey privateKey;
	private PublicKey publicKey;
	private int serialNumber;

	public CompleteCertificate() {
	}

	public CompleteCertificate(CertificatePerson certificatePerson, PublicKey publicKey, PrivateKey privateKey, int serialNumber,
			Certificate certificate) {
		setCertificate(certificate);
		setCertificatePerson(certificatePerson);
		setPrivateKey(privateKey);
		setPublicKey(publicKey);
		setSerialNumber(serialNumber);
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
		CompleteCertificate other = (CompleteCertificate) obj;
		EqualsBuilder builder = new EqualsBuilder();
		builder.append(this.alias, other.alias);
		builder.append(this.certificate, other.certificate);
		builder.append(this.certificatePerson, other.certificatePerson);
		builder.append(this.keyEntry, other.keyEntry);
		builder.append(this.password, other.password);
		builder.append(this.serialNumber, other.serialNumber);
		builder.append(this.privateKey, other.privateKey);
		builder.append(this.serialNumber, other.serialNumber);
		builder.append(this.publicKey, other.publicKey);
		return builder.isEquals();
	}

	public String getAlias() {
		return this.alias;
	}

	public Certificate getCertificate() {
		return this.certificate;
	}

	public CertificatePerson getCertificatePerson() {
		return this.certificatePerson;
	}

	public CompleteCertificate getIssuer() {
		return this.issuer;
	}

	public KeystoreFile getKeystoreFile() {
		return this.keystoreFile;
	}

	public String getPassword() {
		return this.password;
	}

	public PrivateKey getPrivateKey() {
		return this.privateKey;
	}

	public PublicKey getPublicKey() {
		return this.publicKey;
	}

	public int getSerialNumber() {
		return this.serialNumber;
	}

	public X509Certificate getX509Certificate() {
		if (this.certificate instanceof X509Certificate) {
			return (X509Certificate) this.certificate;
		}
		return null;
	}

	@Override
	public int hashCode() {
		int result = 1;
		result = 31 * result + (this.alias == null ? 0 : this.alias.hashCode());
		result = 31 * result + (this.certificate == null ? 0 : this.certificate.hashCode());
		result = 31 * result + (this.certificatePerson == null ? 0 : this.certificatePerson.hashCode());
		result = 31 * result + (this.keyEntry ? 1231 : 1237);
		result = 31 * result + (this.password == null ? 0 : this.password.hashCode());
		result = 31 * result + this.serialNumber;
		return result;
	}

	public boolean isKeyEntry() {
		return this.keyEntry;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public final void setCertificate(Certificate certificate) {
		this.certificate = certificate;
		if (certificate instanceof X509Certificate) {
			this.certificatePerson = new CertificatePerson();
			X509Certificate x509 = (X509Certificate) certificate;
			this.certificatePerson.setName(x509.getSubjectX500Principal().getName());
		}
	}

	public final void setCertificatePerson(CertificatePerson certificatePerson) {
		this.certificatePerson = certificatePerson;
	}

	public void setIssuer(CompleteCertificate issuer) {
		this.issuer = issuer;
	}

	public void setKeyEntry(boolean keyEntry) {
		this.keyEntry = keyEntry;
	}

	public void setKeystoreFile(KeystoreFile keystoreFile) {
		this.keystoreFile = keystoreFile;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public final void setPrivateKey(PrivateKey privateKey) {
		this.privateKey = privateKey;
		if (privateKey != null) {
			setKeyEntry(true);
		}
	}

	public final void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}

	public final void setSerialNumber(int serialNumber) {
		this.serialNumber = serialNumber;
	}
}
