package eu.arrowhead.common.messages;

import eu.arrowhead.common.database.ArrowheadSystem;

public class ConnectToConsumerRequest {

  private String brokerName;
  private int brokerPort;
  private String queueName;
  private String controlQueueName;
  private ArrowheadSystem consumer;
  private Boolean isSecure;
  private int timeout;
  private String providerGWPublicKey;

  public ConnectToConsumerRequest() {
  }

  public ConnectToConsumerRequest(String brokerName, Integer brokerPort, String queueName, String controlQueueName,
      ArrowheadSystem consumer, Boolean isSecure, int timeout, String providerGWPublicKey) {
    this.brokerName = brokerName;
    this.brokerPort = brokerPort;
    this.queueName = queueName;
    this.controlQueueName = controlQueueName;
    this.consumer = consumer;
    this.isSecure = isSecure;
    this.providerGWPublicKey = providerGWPublicKey;
    this.timeout = timeout;
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

  public String getControlQueueName() {
    return controlQueueName;
  }

  public void setControlQueueName(String controlQueueName) {
    this.controlQueueName = controlQueueName;
  }

  public ArrowheadSystem getConsumer() {
    return consumer;
  }

  public void setConsumer(ArrowheadSystem consumer) {
    this.consumer = consumer;
  }

  public Boolean getIsSecure() {
    return isSecure;
  }

  public void setIsSecure(Boolean isSecure) {
    this.isSecure = isSecure;
  }

  public int getTimeout() {
    return timeout;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  public String getProviderGWPublicKey() {
    return providerGWPublicKey;
  }

  public void setProviderGWPublicKey(String providerGWPublicKey) {
    this.providerGWPublicKey = providerGWPublicKey;
  }

}