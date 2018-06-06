/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.messages;

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
