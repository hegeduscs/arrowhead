package eu.arrowhead.common.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.ServiceConfigurationError;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import org.apache.log4j.Logger;

public final class SecurityUtils {

  private static Logger log = Logger.getLogger(SecurityUtils.class.getName());

  public static KeyStore loadKeyStore(String filePath, String pass) {
    File file = new File(filePath);

    try {
      KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
      FileInputStream is = new FileInputStream(file);
      keystore.load(is, pass.toCharArray());
      is.close();
      return keystore;
    } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
      e.printStackTrace();
      log.fatal("Loading the keystore failed: " + e.toString() + " " + e.getMessage());
      throw new ServiceConfigurationError("Loading the keystore failed...", e);
    }
  }

  public static X509Certificate getFirstCertFromKeyStore(KeyStore keystore) {
    try {
      Enumeration<String> enumeration = keystore.aliases();
      String alias = enumeration.nextElement();
      Certificate certificate = keystore.getCertificate(alias);
      return (X509Certificate) certificate;
    } catch (KeyStoreException | NoSuchElementException e) {
      e.printStackTrace();
      log.error("Getting the first cert from keystore failed: " + e.toString() + " " + e.getMessage());
      throw new ServiceConfigurationError("Getting the first cert from keystore failed...", e);
    }
  }

  public static String getCertCNFromSubject(String subjectname) {
    String cn = null;
    try {
      // Subject is in LDAP format, we can use the LdapName object for parsing
      LdapName ldapname = new LdapName(subjectname);
      for (Rdn rdn : ldapname.getRdns()) {
        // Find the data after the CN field
        if (rdn.getType().equalsIgnoreCase("CN")) {
          cn = (String) rdn.getValue();
        }
      }
    } catch (InvalidNameException e) {
      System.out.println("InvalidNameException in getCertCNFromSubject: " + e.getMessage());
      return "";
    }

    if (cn == null) {
      return "";
    }

    return cn;
  }

  public static PrivateKey getPrivateKey(KeyStore keystore, String pass) {
    PrivateKey privatekey = null;
    String element;
    try {
      Enumeration<String> enumeration = keystore.aliases();
      while (enumeration.hasMoreElements()) {
        element = enumeration.nextElement();
        privatekey = (PrivateKey) keystore.getKey(element, pass.toCharArray());
        if (privatekey != null) {
          break;
        }
      }
    } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
      e.printStackTrace();
      log.error("Getting the private key from keystore failed: " + e.toString() + " " + e.getMessage());
      throw new ServiceConfigurationError("Getting the private key from keystore failed...", e);
    }

    if (privatekey == null) {
      throw new ServiceConfigurationError("Getting the private key failed, keystore aliases do not identify a key.");
    }
    return privatekey;
  }

  public static X509Certificate getCertFromKeyStore(KeyStore keystore, String name) {
    Enumeration<String> enumeration;
    try {
      enumeration = keystore.aliases();
    } catch (KeyStoreException e) {
      log.error("getCertFromKeyStore throws KeyStoreException");
      return null;
    }

    while (enumeration.hasMoreElements()) {
      String alias = enumeration.nextElement();

      X509Certificate clientCert;
      try {
        clientCert = (X509Certificate) keystore.getCertificate(alias);
      } catch (KeyStoreException e) {
        log.error("getCertFromKeyStore throws KeyStoreException when loading cert: " + alias);
        continue;
      }
      String clientCertCN = getCertCNFromSubject(clientCert.getSubjectDN().getName());

      if (!clientCertCN.equals(name)) {
        continue;
      }
      return clientCert;
    }

    return null;
  }

  public static String getKeyEncoded(Key key) {
    if (key == null) {
      return "";
    }

    byte[] encpub = key.getEncoded();
    StringBuilder sb = new StringBuilder(encpub.length * 2);
    for (byte b : encpub) {
      sb.append(String.format("%02x", b & 0xff));
    }
    return sb.toString();
  }

  public static String getByteEncoded(byte[] array) {
    StringBuilder sb = new StringBuilder(array.length * 2);
    for (byte b : array) {
      sb.append(String.format("%02X", b & 0xff));
    }
    return sb.toString();
  }

}
