package de.ofterdinger.e4.keytool.internal.ui.editor;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import de.ofterdinger.e4.keytool.internal.certificate.CompleteCertificate;
import java.security.cert.Certificate;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class CertificateInput implements IEditorInput {
  private final CompleteCertificate completeCertificate;

  public CertificateInput(Certificate certificate, String name) {
    this.completeCertificate = new CompleteCertificate();
    this.completeCertificate.setCertificate(certificate);
    this.completeCertificate.setAlias(name);
  }

  public CertificateInput(CompleteCertificate completeCertificate) {
    this.completeCertificate = completeCertificate;
  }

  @Override
  public final boolean equals(Object obj) {
    if (super.equals(obj)) {
      return true;
    }
    if (!(obj instanceof CertificateInput)) {
      return false;
    }
    CertificateInput other = (CertificateInput) obj;
    return this.completeCertificate.getCertificate().equals(other.getCertificate());
  }

  @Override
  public final boolean exists() {
    return true;
  }

  @Override
  public final <T> T getAdapter(Class<T> adapter) {
    return null;
  }

  public final Certificate getCertificate() {
    return this.completeCertificate.getCertificate();
  }

  public final CompleteCertificate getCompleteCertificate() {
    return this.completeCertificate;
  }

  @Override
  public final ImageDescriptor getImageDescriptor() {
    return null;
  }

  @Override
  public final String getName() {
    return this.completeCertificate.getAlias();
  }

  @Override
  public final IPersistableElement getPersistable() {
    return null;
  }

  @Override
  public final String getToolTipText() {
    return EMPTY;
  }

  @Override
  public final int hashCode() {
    return this.completeCertificate.getCertificate().hashCode();
  }
}
