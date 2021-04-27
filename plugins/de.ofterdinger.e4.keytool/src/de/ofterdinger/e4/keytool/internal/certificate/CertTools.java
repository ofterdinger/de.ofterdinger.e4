package de.ofterdinger.e4.keytool.internal.certificate;

import static de.ofterdinger.e4.keytool.internal.KeytoolPlugin.PLUGIN_ID;
import static org.eclipse.core.runtime.IStatus.ERROR;

import de.ofterdinger.e4.keytool.internal.KeystoreFile;
import de.ofterdinger.e4.keytool.internal.KeytoolPlugin;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64Encoder;
import org.bouncycastle.x509.X509V1CertificateGenerator;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.eclipse.core.runtime.Status;

public final class CertTools {
  public static final long SIX_YEARS = 189216000000L;
  private static final int FIRST_BYTE_ADDER = 240;
  private static final char[] HEX_CHARS =
      new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
  private static final int NO_OF_ROLLS = 4;
  private static final int RADIX = 16;
  private static final int SECOND_BYTE_ADDER = 15;
  private static X509V1CertificateGenerator v1CertGen = new X509V1CertificateGenerator();

  static {
    Security.addProvider(CertTools.getBouncyCastle());
  }

  private CertTools() {}

  public static void addCertificateAndSaveKeystore(
      CompleteCertificate completeCertificate, KeystoreFile keystoreFile)
      throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
    KeyStore keystore = keystoreFile.getKeystore();
    keystore.setCertificateEntry(
        completeCertificate.getAlias(), completeCertificate.getCertificate());
    Certificate[] chain = CertTools.makeCertificateChain(completeCertificate);
    if (completeCertificate.isKeyEntry()) {
      keystore.setKeyEntry(
          completeCertificate.getAlias(),
          completeCertificate.getPrivateKey(),
          keystoreFile.getPassword().toCharArray(),
          chain);
    }

    try (FileOutputStream fileOut = new FileOutputStream(keystoreFile.getKeystorefilename())) {
      keystore.store(fileOut, keystoreFile.getPassword().toCharArray());
      CertTools.loadKeystoreFile(keystoreFile);
    }
  }

  public static void addCertificateToNewKeystore(
      CompleteCertificate completeCertificate,
      String alias,
      String password,
      String keystorePassword,
      String keystoreFilename,
      KeystoreType keystoreType)
      throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
    KeyStore newKeystore = KeyStore.getInstance(keystoreType.getType());
    newKeystore.load(null, keystorePassword.toCharArray());
    Certificate[] chain = CertTools.makeCertificateChain(completeCertificate);
    newKeystore.setKeyEntry(
        alias, completeCertificate.getPrivateKey(), password.toCharArray(), chain);
    try (FileOutputStream fileOutputStream = new FileOutputStream(keystoreFilename)) {
      newKeystore.store(fileOutputStream, keystorePassword.toCharArray());
      fileOutputStream.flush();
    }
  }

  public static String certificatonRequestAsCSR(PKCS10CertificationRequest request)
      throws IOException {
    byte[] buf = request.getEncoded();
    StringBuilder buff = new StringBuilder();
    buff.append("-----BEGIN NEW CERTIFICATE REQUEST-----\n"); // $NON-NLS-1$
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      new Base64Encoder().encode(buf, 0, buf.length, out);
      buff.append(out.toString());
      buff.append("\n-----END NEW CERTIFICATE REQUEST-----\n"); // $NON-NLS-1$
      return buff.toString();
    }
  }

  public static CompleteCertificate createCertificate(
      CertificatePerson person, Date notBefore, Date notAfter) {
    int serialNumber = makeSerialNumber();
    return CertTools.createCertificate(person, serialNumber, notBefore, notAfter);
  }

  private static CompleteCertificate createCertificate(
      CertificatePerson person, int serialNumber, Date notBefore, Date notAfter) {
    try {
      KeyPair keypair = CertTools.getKeypair();
      PublicKey publicKey = keypair.getPublic();
      PrivateKey privateKey = keypair.getPrivate();
      X509Certificate certificate =
          CertTools.createSelfIssuerCertificate(
              person, publicKey, privateKey, serialNumber, notBefore, notAfter);
      return new CompleteCertificate(person, publicKey, privateKey, serialNumber, certificate);
    } catch (NoSuchAlgorithmException
        | InvalidKeyException
        | SignatureException
        | CertificateException
        | NoSuchProviderException e) {
      KeytoolPlugin.getDefault().getLog().log(new Status(ERROR, PLUGIN_ID, e.getMessage(), e));
      return null;
    }
  }

  public static CompleteCertificate createClientCertificate(
      CertificatePerson person, CompleteCertificate issuer, Date notBefore, Date notAfter)
      throws InvalidKeyException, NoSuchProviderException, SignatureException,
          CertificateEncodingException, IllegalStateException, NoSuchAlgorithmException {
    Hashtable<ASN1ObjectIdentifier, String> attrs = new Hashtable<>();
    Vector<ASN1ObjectIdentifier> order = new Vector<>();
    populate(person, attrs, order);
    Hashtable<ASN1ObjectIdentifier, String> attrsIssuer = new Hashtable<>();
    Vector<ASN1ObjectIdentifier> orderIssuer = new Vector<>();
    populate(issuer.getCertificatePerson(), attrsIssuer, orderIssuer);

    X509V3CertificateGenerator v3CertGen = new X509V3CertificateGenerator();
    v3CertGen.setIssuerDN(new X509Principal(orderIssuer, attrsIssuer));
    v3CertGen.setNotBefore(notBefore);
    v3CertGen.setNotAfter(notAfter);
    v3CertGen.setSubjectDN(new X509Principal(order, attrs));
    v3CertGen.setSignatureAlgorithm("SHA1WithRSAEncryption"); // $NON-NLS-1$

    CompleteCertificate clientCertificate = new CompleteCertificate();
    clientCertificate.setIssuer(issuer);

    CompleteCertificate personCertificate = createCertificate(person, notBefore, notAfter);
    if (personCertificate != null) {
      v3CertGen.setSerialNumber(BigInteger.valueOf(personCertificate.getSerialNumber()));
      v3CertGen.setPublicKey(personCertificate.getPublicKey());
      clientCertificate.setPrivateKey(personCertificate.getPrivateKey());
    }

    X509Certificate cert = v3CertGen.generate(issuer.getPrivateKey(), "BC"); // $NON-NLS-1$
    clientCertificate.setCertificate(cert);

    return clientCertificate;
  }

  public static X509Certificate createSelfIssuerCertificate(
      CertificatePerson person, PublicKey pubKey, PrivateKey privKey, int serialNumber)
      throws InvalidKeyException, SignatureException, CertificateException,
          NoSuchAlgorithmException, NoSuchProviderException {
    Date notBefore = new Date(System.currentTimeMillis());
    Date notAfter = new Date(System.currentTimeMillis() + 189216000000L);
    return CertTools.createSelfIssuerCertificate(
        person, pubKey, privKey, serialNumber, notBefore, notAfter);
  }

  public static X509Certificate createSelfIssuerCertificate(
      CertificatePerson person,
      PublicKey pubKey,
      PrivateKey privKey,
      int serialNumber,
      Date notBefore,
      Date notAfter)
      throws InvalidKeyException, SignatureException, CertificateException,
          NoSuchAlgorithmException, NoSuchProviderException {
    String issuer;
    String subject = issuer = person.toString();
    v1CertGen.setSerialNumber(BigInteger.valueOf(serialNumber));
    v1CertGen.setIssuerDN(new X509Principal(issuer));
    v1CertGen.setNotBefore(notBefore);
    v1CertGen.setNotAfter(notAfter);
    v1CertGen.setSubjectDN(new X509Principal(subject));
    v1CertGen.setPublicKey(pubKey);
    v1CertGen.setSignatureAlgorithm("SHA1WITHRSA"); // $NON-NLS-1$
    X509Certificate cert = v1CertGen.generate(privKey);
    cert.verify(pubKey);
    return cert;
  }

  public static void exportCertificate(Certificate certificate, String filename)
      throws CertificateEncodingException, IOException {
    try (FileOutputStream fos = new FileOutputStream(new File(filename))) {
      fos.write(certificate.getEncoded());
    }
  }

  public static void exportPersonalCertToPFX(CompleteCertificate cc, String filename) {
    CertTools.exportPersonalCertToPFX(
        (X509Certificate) cc.getCertificate(), cc.getPrivateKey(), filename, cc.getPassword());
  }

  public static void exportPersonalCertToPFX(
      X509Certificate cert, PrivateKey privateKey, String filename, String password) {
    try (FileOutputStream fos = new FileOutputStream(filename)) {
      String alias =
          Long.toHexString(SecureRandom.getInstance("SHA1PRNG").nextLong()); // $NON-NLS-1$
      KeyStore ks = KeyStore.getInstance("PKCS12", "BC"); // $NON-NLS-1$ //$NON-NLS-2$
      char[] pwdArray = password.toCharArray();
      ks.load(null, pwdArray);
      Certificate[] certs = new X509Certificate[] {cert};
      PrivateKey key = privateKey;
      if (key != null) {
        ks.setKeyEntry(alias, key, pwdArray, certs);
      }
      ks.store(fos, pwdArray);
    } catch (Exception e) {
      KeytoolPlugin.getDefault().getLog().log(new Status(ERROR, PLUGIN_ID, e.getMessage(), e));
    }
  }

  public static PKCS10CertificationRequest generateCertificateRequest(CompleteCertificate complete)
      throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException,
          SignatureException {
    PKCS10CertificationRequest request = null;
    X509Principal x509Principal =
        new X509Principal(complete.getX509Certificate().getSubjectDN().getName());
    if (complete.getPublicKey() != null || complete.getPrivateKey() != null) {
      request =
          new PKCS10CertificationRequest(
              "SHA1WITHRSA",
              x509Principal,
              complete.getPublicKey(),
              null, //$NON-NLS-1$
              complete.getPrivateKey());
      if (!request.verify()) {
        KeytoolPlugin.getDefault()
            .getLog()
            .log(
                new Status(
                    ERROR,
                    PLUGIN_ID,
                    "The certificate request verification failed")); //$NON-NLS-1$
      }
    } else {
      throw new IllegalArgumentException(
          "Could not generate CSR missing public/private key"); //$NON-NLS-1$
    }
    return request;
  }

  public static String generateCSR(CompleteCertificate complete)
      throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException,
          SignatureException {
    return CertTools.certificatonRequestAsCSR(CertTools.generateCertificateRequest(complete));
  }

  public static byte[] generateFingerprint(byte[] ba, Algorithm algorithm) {
    try {
      MessageDigest md = MessageDigest.getInstance(algorithm.getName());
      return md.digest(ba);
    } catch (NoSuchAlgorithmException e) {
      KeytoolPlugin.getDefault().getLog().log(new Status(ERROR, PLUGIN_ID, e.getMessage(), e));
      return new byte[0];
    }
  }

  public static String generateFingerprintInHex(byte[] ba, Algorithm algorithm) {
    return CertTools.hexToString(CertTools.generateFingerprint(ba, algorithm));
  }

  public static String generateFingerprintInHex(Certificate certificate, Algorithm algorithm)
      throws CertificateEncodingException {
    return CertTools.hexToString(
        CertTools.generateFingerprint(certificate.getEncoded(), algorithm));
  }

  public static String generateFingerprintInHex(
      KeystoreFile file, String alias, Algorithm algorithm)
      throws KeyStoreException, CertificateEncodingException {
    Certificate certificate = CertTools.getCertificate(file, alias).getCertificate();
    if (certificate != null) {
      return CertTools.generateFingerprintInHex(certificate.getEncoded(), algorithm);
    }
    return null;
  }

  public static CompleteCertificate getCertificate(KeystoreFile file, String alias)
      throws KeyStoreException {
    CompleteCertificate c = new CompleteCertificate();
    c.setCertificate(file.getKeystore().getCertificate(alias));
    c.setKeyEntry(file.getKeystore().isKeyEntry(alias));
    c.setAlias(alias);
    c.setKeystoreFile(file);
    return c;
  }

  public static Certificate[] getCertificateChain(CompleteCertificate completeCertificate)
      throws KeyStoreException {
    return completeCertificate
        .getKeystoreFile()
        .getKeystore()
        .getCertificateChain(completeCertificate.getAlias());
  }

  public static CompleteCertificate getCompleteCertificate(
      KeystoreFile file, String alias, String certificatePassword)
      throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException,
          InvalidKeySpecException {
    KeyStore keystore = file.getKeystore();
    Certificate certificate = keystore.getCertificate(alias);
    CompleteCertificate complete = new CompleteCertificate();
    complete.setCertificate(certificate);
    complete.setKeyEntry(keystore.isKeyEntry(alias));
    if (complete.isKeyEntry()) {
      Key key = keystore.getKey(alias, certificatePassword.toCharArray());
      PrivateKey pKey = CertTools.makePrivateKey(key.getEncoded());
      complete.setPrivateKey(pKey);
    }
    complete.setPassword(certificatePassword);
    complete.setKeystoreFile(file);
    complete.setAlias(alias);
    complete.setPublicKey(certificate.getPublicKey());
    return complete;
  }

  public static String getSerialNumber(Certificate certificate) {
    if (certificate instanceof X509Certificate) {
      X509Certificate x509certificate = (X509Certificate) certificate;
      return x509certificate.getSerialNumber().toString(RADIX);
    }
    return null;
  }

  public static String hexToString(byte[] buffer) {
    StringBuilder hexString = new StringBuilder(2 * buffer.length);
    int i = 0;
    while (i < buffer.length) {
      CertTools.appendHexPair(buffer[i], hexString);
      ++i;
    }
    return hexString.toString();
  }

  public static Certificate loadCertificate(InputStream inputStream) throws CertificateException {
    CertificateFactory cf = CertificateFactory.getInstance("X.509"); // $NON-NLS-1$
    return cf.generateCertificate(inputStream);
  }

  public static Certificate loadCertificate(String filename)
      throws CertificateException, IOException {
    try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filename))) {
      return CertTools.loadCertificate(bis);
    }
  }

  public static void loadKeystoreFile(KeystoreFile keystoreFile)
      throws NoSuchAlgorithmException, CertificateException, IOException {
    String keystoreFilename = keystoreFile.getKeystorefilename();
    String keystorePassword = keystoreFile.getPassword();
    try (FileInputStream fileIn = new FileInputStream(keystoreFilename)) {
      keystoreFile.getKeystore().load(fileIn, keystorePassword.toCharArray());
    }
  }

  public static KeystoreFile loadKeystoreFile(
      String keystoreFilename, KeystoreType keystoreType, String keystorePassword)
      throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
    KeyStore keystore = KeyStore.getInstance(keystoreType.getType());
    try (FileInputStream fileIn = new FileInputStream(keystoreFilename)) {
      keystore.load(fileIn, keystorePassword.toCharArray());
      return new KeystoreFile(keystore, keystoreFilename, keystorePassword);
    }
  }

  public static CompleteCertificate loadPFX(String filename, String password)
      throws KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException,
          CertificateException, IOException, UnrecoverableKeyException, InvalidKeySpecException {
    KeyStore ks = KeyStore.getInstance("PKCS12", "BC"); // $NON-NLS-1$ //$NON-NLS-2$
    try (FileInputStream fis = new FileInputStream(filename)) {
      ks.load(fis, password.toCharArray());
    }
    Enumeration<String> aliases = ks.aliases();
    if (aliases.hasMoreElements()) {
      String alias = aliases.nextElement();
      Certificate certificate = ks.getCertificate(alias);
      Key key = ks.getKey(alias, password.toCharArray());
      CompleteCertificate complete = new CompleteCertificate();
      complete.setCertificate(certificate);
      complete.setPrivateKey(CertTools.makePrivateKey(key.getEncoded()));
      return complete;
    }
    return null;
  }

  private static void appendHexPair(byte b, StringBuilder hexString) {
    char firstByte = HEX_CHARS[(b & FIRST_BYTE_ADDER) >> NO_OF_ROLLS];
    char secondByte = HEX_CHARS[b & SECOND_BYTE_ADDER];
    if (hexString.length() > 0) {
      hexString.append(":"); // $NON-NLS-1$
    }
    hexString.append(firstByte);
    hexString.append(secondByte);
  }

  private static Provider getBouncyCastle() {
    return new BouncyCastleProvider();
  }

  private static KeyPair getKeypair() throws NoSuchAlgorithmException {
    SecureRandom sr = new SecureRandom();
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA"); // $NON-NLS-1$
    keyGen.initialize(2048, sr);
    return keyGen.generateKeyPair();
  }

  private static Certificate[] makeCertificateChain(CompleteCertificate completeCertificate) {
    Certificate[] chain;
    if (completeCertificate.getIssuer() == null) {
      chain = new Certificate[] {completeCertificate.getCertificate()};
    } else {
      ArrayList<Certificate> chains = new ArrayList<>();
      chains.add(completeCertificate.getCertificate());
      CompleteCertificate issuer = completeCertificate.getIssuer();
      while (issuer != null) {
        chains.add(issuer.getCertificate());
        issuer = issuer.getIssuer();
      }
      chain = chains.toArray(new Certificate[chains.size()]);
    }
    return chain;
  }

  private static PrivateKey makePrivateKey(byte[] encoded)
      throws InvalidKeySpecException, NoSuchAlgorithmException {
    KeyFactory rSAKeyFactory = KeyFactory.getInstance("RSA"); // $NON-NLS-1$
    return rSAKeyFactory.generatePrivate(new PKCS8EncodedKeySpec(encoded));
  }

  private static int makeSerialNumber() {
    int serialNumber = (int) System.currentTimeMillis();
    if (serialNumber < 0) {
      serialNumber *= -1;
    }
    return serialNumber;
  }

  private static void populate(
      CertificatePerson person,
      Hashtable<ASN1ObjectIdentifier, String> attrs,
      List<ASN1ObjectIdentifier> order) {
    if (person.getCountryCode() != null) {
      order.add(X509Name.C);
      attrs.put(X509Name.C, person.getCountryCode());
    }
    if (person.getOrganization() != null) {
      order.add(X509Name.O);
      attrs.put(X509Name.O, person.getOrganization());
    }
    if (person.getLocation() != null) {
      order.add(X509Name.L);
      attrs.put(X509Name.L, person.getLocation());
    }
    if (person.getName() != null) {
      order.add(X509Name.CN);
      attrs.put(X509Name.CN, person.getName());
    }
    if (person.getEmail() != null) {
      order.add(X509Name.EmailAddress);
      attrs.put(X509Name.EmailAddress, person.getEmail());
    }
  }
}
