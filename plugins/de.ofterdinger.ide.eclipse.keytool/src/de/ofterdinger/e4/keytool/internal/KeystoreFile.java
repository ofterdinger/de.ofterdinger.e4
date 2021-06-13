package de.ofterdinger.e4.keytool.internal;

import de.ofterdinger.e4.keytool.internal.certificate.KeystoreType;
import java.security.KeyStore;

public class KeystoreFile {
  private final KeyStore keystore;
  private final String keystorefilename;
  private final String password;

  public KeystoreFile(KeyStore keystore, String keystorefilename, String password) {
    this.keystore = keystore;
    this.keystorefilename = keystorefilename;
    this.password = password;
  }

  public KeyStore getKeystore() {
    return this.keystore;
  }

  public String getKeystorefilename() {
    return this.keystorefilename;
  }

  public KeystoreType getKeystoreType() {
    if (this.keystore == null) {
      return null;
    }
    return KeystoreType.getInstance(this.keystore.getType());
  }

  public String getPassword() {
    return this.password;
  }
}
