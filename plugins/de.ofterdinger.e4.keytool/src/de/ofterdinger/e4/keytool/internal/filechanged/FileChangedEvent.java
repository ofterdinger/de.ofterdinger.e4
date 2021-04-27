package de.ofterdinger.e4.keytool.internal.filechanged;

import de.ofterdinger.e4.keytool.internal.certificate.KeystoreType;

public class FileChangedEvent {
  public static final int FILE_ADDED = 10;
  public static final int FILE_REMOVED = 11;
  public static final int FILE_UPDATED = 12;
  private final String filename;
  private final KeystoreType keystoreType;
  private final int operation;
  private final String password;

  public FileChangedEvent(int theOperation, String theFilename) {
    this(theOperation, theFilename, null, null);
  }

  public FileChangedEvent(
      int theOperation, String theFilename, String password, KeystoreType keystoreType) {
    this.operation = theOperation;
    this.filename = theFilename;
    this.password = password;
    this.keystoreType = keystoreType;
  }

  public final String getFileName() {
    return this.filename;
  }

  public final KeystoreType getKeystoreType() {
    return this.keystoreType;
  }

  public final int getOperationType() {
    return this.operation;
  }

  public final String getPassword() {
    return this.password;
  }

  @Override
  public final String toString() {
    String retVal = null;
    switch (getOperationType()) {
      case FILE_ADDED:
        retVal = "FILE_ADDED "; 
        break;
      case FILE_REMOVED:
        retVal = "FILE_REMOVED "; 
        break;
      case FILE_UPDATED:
        retVal = "FILE_UPDATED "; 
        break;
      default:
        retVal = "<Unknown> "; 
    }
    return String.valueOf(retVal) + getFileName();
  }
}
