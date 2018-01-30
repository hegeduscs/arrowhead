package eu.arrowhead.common.messages;

import eu.arrowhead.common.database.ArrowheadCloud;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;

public class ConnectToProviderRequest {

  private String brokerName;
  private int brokerPort;
  private ArrowheadSystem consumer;
  private ArrowheadSystem provider;
  private ArrowheadCloud consumerCloud;
  private ArrowheadCloud providerCloud;
  private ArrowheadService service;
  private boolean isSecure;
  private int timeout;
  private String consumerGWPublicKey;

  public ConnectToProviderRequest() {
  }

  public ConnectToProviderRequest(String brokerName, int brokerPort, ArrowheadSystem consumer, ArrowheadSystem provider, ArrowheadCloud consumerCloud,
                                  ArrowheadCloud providerCloud, ArrowheadService service, boolean isSecure, int timeout, String consumerGWPublicKey) {
    this.brokerName = brokerName;
    this.brokerPort = brokerPort;
    this.consumer = consumer;
    this.provider = provider;
    this.consumerCloud = consumerCloud;
    this.providerCloud = providerCloud;
    this.service = service;
    this.isSecure = isSecure;
    this.timeout = timeout;
    this.consumerGWPublicKey = consumerGWPublicKey;
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

  public boolean getIsSecure() {
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

  public String getConsumerGWPublicKey() {
    return consumerGWPublicKey;
  }

  public void setConsumerGWPublicKey(String consumerGWPublicKey) {
    this.consumerGWPublicKey = consumerGWPublicKey;
  }

}
