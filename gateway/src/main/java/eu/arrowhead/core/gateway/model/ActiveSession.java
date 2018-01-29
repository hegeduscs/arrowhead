package eu.arrowhead.core.gateway.model;

import eu.arrowhead.common.database.ArrowheadCloud;
import eu.arrowhead.common.database.ArrowheadSystem;

public class ActiveSession {
  private ArrowheadSystem consumer;
  private ArrowheadSystem provider;
  private ArrowheadCloud consumerCloud;
  private ArrowheadCloud providerCloud;
  private String brokerName;
  private int brokerPort;
  private int serverSocketPort;
  private String queueName;
  private String controlQueueName;
  private Boolean isSecure;
  private String date;

  public ActiveSession() {
  }

  public ActiveSession(ArrowheadSystem consumer, ArrowheadSystem provider, ArrowheadCloud consumerCloud,
      ArrowheadCloud providerCloud, String brokerName, int brokerPort, int serverSocketPort, String queueName,
      String controlQueueName, Boolean isSecure, String date) {
    this.consumer = consumer;
    this.provider = provider;
    this.consumerCloud = consumerCloud;
    this.providerCloud = providerCloud;
    this.brokerName = brokerName;
    this.brokerPort = brokerPort;
    this.serverSocketPort = serverSocketPort;
    this.queueName = queueName;
    this.controlQueueName = controlQueueName;
    this.isSecure = isSecure;
    this.date = date;
  }

  public ActiveSession(ArrowheadSystem consumer, String brokerName, int brokerPort, int serverSocketPort,
      Boolean isSecure) {
    this.consumer = consumer;
    this.brokerName = brokerName;
    this.brokerPort = brokerPort;
    this.serverSocketPort = serverSocketPort;
    this.isSecure = isSecure;
  }

  public ArrowheadSystem getConsumer() {
    return consumer;
  }

  public void setConsumer(ArrowheadSystem consumer) {
    this.consumer = consumer;
  }

  public ArrowheadSystem getProvider() {
    return provider;
  }

  public void setProvider(ArrowheadSystem provider) {
    this.provider = provider;
  }

  public ArrowheadCloud getConsumerCloud() {
    return consumerCloud;
  }

  public void setConsumerCloud(ArrowheadCloud consumerCloud) {
    this.consumerCloud = consumerCloud;
  }

  public ArrowheadCloud getProviderCloud() {
    return providerCloud;
  }

  public void setProviderCloud(ArrowheadCloud providerCloud) {
    this.providerCloud = providerCloud;
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

  public int getServerSocketPort() {
    return serverSocketPort;
  }

  public void setServerSocketPort(int serverSocketPort) {
    this.serverSocketPort = serverSocketPort;
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

  public Boolean getIsSecure() {
    return isSecure;
  }

  public void setIsSecure(Boolean isSecure) {
    this.isSecure = isSecure;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

}
