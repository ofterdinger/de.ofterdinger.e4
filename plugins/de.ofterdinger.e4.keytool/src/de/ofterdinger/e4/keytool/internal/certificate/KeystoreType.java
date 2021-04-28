package de.ofterdinger.e4.keytool.internal.certificate;

public final class KeystoreType {
  public static final KeystoreType JKS = new KeystoreType("JKS");
  public static final KeystoreType PKCS12 = new KeystoreType("PKCS12");
  private static final KeystoreType[] TYPES = new KeystoreType[] {JKS, PKCS12};

  private final String type;

  private KeystoreType(String keystoreType) {
    this.type = keystoreType;
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
    KeystoreType other = (KeystoreType) obj;
    return (this.type == null ? other.type != null : !this.type.equals(other.type));
  }

  public String getType() {
    return this.type;
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + (this.type == null ? 0 : this.type.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return this.type;
  }

  public static KeystoreType getInstance(String keystoreTypeAsString) {
    KeystoreType[] arrkeystoreType = TYPES;
    int n = arrkeystoreType.length;
    int n2 = 0;
    while (n2 < n) {
      KeystoreType keystoreType = arrkeystoreType[n2];
      if (keystoreType.getType().equalsIgnoreCase(keystoreTypeAsString)) {
        return keystoreType;
      }
      ++n2;
    }
    throw new IllegalArgumentException("KeystoreType not supported");
  }

  public static KeystoreType[] getTypes() {
    return TYPES;
  }
}
