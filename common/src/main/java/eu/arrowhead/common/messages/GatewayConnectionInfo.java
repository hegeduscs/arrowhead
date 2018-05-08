/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.messages;

public class GatewayConnectionInfo {

  private String brokerName;
  private int brokerPort;
  private String queueName;
  private String controlQueueName;
  private String gatewayPublicKey;

  public GatewayConnectionInfo() {
  }

  public GatewayConnectionInfo(String brokerName, int brokerPort, String queueName, String controlQueueName, String gatewayPublicKey) {
    this.brokerName = brokerName;
    this.brokerPort = brokerPort;
    this.queueName = queueName;
    this.controlQueueName = controlQueueName;
    this.gatewayPublicKey = gatewayPublicKey;
  }

  public String getBrokerName() {
    return brokerName;
  }

  public void setBrokerName(String brokerName) {
    this.brokerName = brokerName;
  }

  public int getBrokerPort() {
    return brokerPort;
  }

  public void setBrokerPort(int brokerPort) {
    this.brokerPort = brokerPort;
  }

  public String getQueueName() {
    return queueName;
  }

  public void setQueueName(String queueName) {
    this.queueName = queueName;
  }

  public void setControlQueueName(String controlQueueName) {
    this.controlQueueName = controlQueueName;
  }

  public String getControlQueueName() {
    return controlQueueName;
  }

  public String getGatewayPublicKey() {
    return gatewayPublicKey;
  }

  public void setGatewayPublicKey(String gatewayPublicKey) {
    this.gatewayPublicKey = gatewayPublicKey;
  }

}
