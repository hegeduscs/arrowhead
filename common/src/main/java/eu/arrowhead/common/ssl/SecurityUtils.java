package eu.arrowhead.common.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

public final class SecurityUtils {

  public static KeyStore loadKeyStore(String filePath, String pass) throws Exception {

    File tempFile = new File(filePath);
    FileInputStream is = null;
    KeyStore keystore = null;

    try {
      keystore = KeyStore.getInstance(KeyStore.getDefaultType());
      is = new FileInputStream(tempFile);
      keystore.load(is, pass.toCharArray());
    } catch (KeyStoreException e) {
      throw new Exception("In Utils::loadKeyStore, KeyStoreException occured: " + e.toString());
    } catch (FileNotFoundException e) {
      throw new Exception("In Utils::loadKeyStore, FileNotFoundException occured: " + e.toString());
    } catch (NoSuchAlgorithmException e) {
      throw new Exception(
          "In Utils::loadKeyStore, NoSuchAlgorithmException occured: " + e.toString());
    } catch (CertificateException e) {
      throw new Exception("In Utils::loadKeyStore, CertificateException occured: " + e.toString());
    } catch (IOException e) {
      throw new Exception("In Utils::loadKeyStore, IOException occured: " + e.toString());
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          throw new Exception("In Utils::loadKeyStore, IOException occured: " + e.toString());
        }
      }
    }

    return keystore;
  }

  public static X509Certificate getFirstCertFromKeyStore(KeyStore keystore) throws Exception {

    X509Certificate xCert = null;
    Enumeration<String> enumeration;
    try {
      enumeration = keystore.aliases();

      if (enumeration.hasMoreElements()) {
        String alias = enumeration.nextElement();
        Certificate certificate = keystore.getCertificate(alias);
        xCert = (X509Certificate) certificate;
      } else {
        throw new Exception("Error: no certificate was in keystore!");
      }
    } catch (KeyStoreException e) {
      throw new Exception("KeyStoreException occured: " + e.toString());
    }

    return xCert;
  }

  public static X509Certificate getCertFromKeyStore(KeyStore keystore, String name) {

    Enumeration<String> enumeration;
    try {
      enumeration = keystore.aliases();
    } catch (KeyStoreException e) {
      System.out.println("Error in Utils::getCertFromKeyStore(): " + e.toString());
      return null;
    }

    while (enumeration.hasMoreElements()) {
      String alias = enumeration.nextElement();

      X509Certificate clientCert = null;
      try {
        clientCert = (X509Certificate) keystore.getCertificate(alias);
      } catch (KeyStoreException e) {
        System.out.println("Error, cannot load certificate " + alias + " in keystore, skipping...");
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

  public static PrivateKey getPrivateKey(KeyStore keystore, String pass) throws Exception {
    Enumeration<String> enumeration = null;
    PrivateKey privatekey = null;
    String elem;
    try {
      enumeration = keystore.aliases();
      while (true) {
        if (!enumeration.hasMoreElements()) {
          throw new Exception("Error: no elements in keystore!");
        }
        elem = enumeration.nextElement();
        privatekey = (PrivateKey) keystore.getKey(elem, pass.toCharArray());
        if (privatekey != null) {
          break;
        }
      }
    } catch (Exception e) {
      throw new Exception("Error in Utils::getPrivateKey(): " + e.toString());
    }

    if (privatekey == null) {
      throw new Exception("Error in Utils::getPrivateKey(): no private key "
                              + "returned for alias: " + elem + " ,pass: " + pass);
    }

    return privatekey;
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
      System.out.println("Exception in getCertCN: " + e.toString());
      return "";
    }

    if (cn == null) {
      return "";
    }

    return cn;
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
