package de.ofterdinger.e4.keytool.internal.certificate;

public class Algorithm {
  public static final Algorithm MD5 = new Algorithm("MD5");
  public static final Algorithm SHA1 = new Algorithm("SHA1");

  private final String name;

  private Algorithm(String algorithm) {
    this.name = algorithm;
  }

  public String getName() {
    return this.name;
  }
}
