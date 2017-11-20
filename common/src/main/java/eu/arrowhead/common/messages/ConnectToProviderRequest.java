package eu.arrowhead.common.messages;

import eu.arrowhead.common.database.ArrowheadSystem;

public class ConnectToProviderRequest {

  private String brokerHost;
  private int brokerPort;
  private ArrowheadSystem provider;
  private boolean isSecure;
  private int timeout;

  public ConnectToProviderRequest() {
  }

  public ConnectToProviderRequest(String brokerHost, int brokerPort, ArrowheadSystem provider, boolean isSecure, int timeout) {
    this.brokerHost = brokerHost;
    this.brokerPort = brokerPort;
    this.provider = provider;
    this.isSecure = isSecure;
    this.timeout = timeout;
  }

  public String getBrokerHost() {
    return brokerHost;
  }

  public void setBrokerHost(String brokerHost) {
    this.brokerHost = brokerHost;
  }

  public int getBrokerPort() {
    return brokerPort;
  }

  public void setBrokerPort(int brokerPort) {
    this.brokerPort = brokerPort;
  }

  public ArrowheadSystem getProvider() {
    return provider;
  }

  public void setProvider(ArrowheadSystem provider) {
    this.provider = provider;
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


}
