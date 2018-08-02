package eu.arrowhead.common.messages;

public class CertificateInfo {

  private String commonName;
  private String publicKey;
  private String privateKey;

  public CertificateInfo() {
  }

  public CertificateInfo(String commonName, String publicKey, String privateKey) {
    this.commonName = commonName;
    this.publicKey = publicKey;
    this.privateKey = privateKey;
  }

  public String getCommonName() {
    return commonName;
  }

  public void setCommonName(String commonName) {
    this.commonName = commonName;
  }

  public String getPublicKey() {
    return publicKey;
  }

  public void setPublicKey(String publicKey) {
    this.publicKey = publicKey;
  }

  public String getPrivateKey() {
    return privateKey;
  }

  public void setPrivateKey(String privateKey) {
    this.privateKey = privateKey;
  }
}
