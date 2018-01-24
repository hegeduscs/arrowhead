package eu.arrowhead.core.gateway.model;

public class GatewayEncryption {

  private byte[] encryptedAESKey;
  private byte[] encryptedIVAndMessage;

  public GatewayEncryption() {
  }

  public GatewayEncryption(byte[] encryptedAESKey, byte[] encryptedIVAndMessage) {
    this.encryptedAESKey = encryptedAESKey;
    this.encryptedIVAndMessage = encryptedIVAndMessage;
  }

  public byte[] getEncryptedAESKey() {
    return encryptedAESKey;
  }

  public void setEncryptedAESKey(byte[] encryptedAESKey) {
    this.encryptedAESKey = encryptedAESKey;
  }

  public byte[] getEncryptedIVAndMessage() {
    return encryptedIVAndMessage;
  }

  public void setEncryptedIVAndMessage(byte[] encryptedIVAndMessage) {
    this.encryptedIVAndMessage = encryptedIVAndMessage;
  }

}
