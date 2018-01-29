package eu.arrowhead.common.messages;

import eu.arrowhead.common.database.ArrowheadCloud;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;

public class ConnectToConsumerRequest {

  private String brokerName;
  private int brokerPort;
  private String queueName;
  private String controlQueueName;
  private ArrowheadSystem consumer;
  private ArrowheadSystem provider;
  private ArrowheadCloud consumerCloud;
  private ArrowheadCloud providerCloud;
  private ArrowheadService service;
  private Boolean isSecure;
  private Boolean useToken;
  private int timeout;
  private String providerGWPublicKey;

  public ConnectToConsumerRequest() {
  }

  public ConnectToConsumerRequest(String brokerName, int brokerPort, String queueName, String controlQueueName,
      ArrowheadSystem consumer, ArrowheadSystem provider, ArrowheadCloud consumerCloud, ArrowheadCloud providerCloud,
      ArrowheadService service, Boolean isSecure, Boolean useToken, int timeout, String providerGWPublicKey) {
    this.brokerName = brokerName;
    this.brokerPort = brokerPort;
    this.queueName = queueName;
    this.controlQueueName = controlQueueName;
    this.consumer = consumer;
    this.provider = provider;
    this.consumerCloud = consumerCloud;
    this.providerCloud = providerCloud;
    this.service = service;
    this.isSecure = isSecure;
    this.useToken = useToken;
    this.timeout = timeout;
    this.providerGWPublicKey = providerGWPublicKey;
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

  public ArrowheadService getService() {
    return service;
  }

  public void setService(ArrowheadService service) {
    this.service = service;
  }

  public Boolean getIsSecure() {
    return isSecure;
  }

  public void setIsSecure(Boolean isSecure) {
    this.isSecure = isSecure;
  }

  public Boolean getUseToken() {
    return useToken;
  }

  public void setUseToken(Boolean useToken) {
    this.useToken = useToken;
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