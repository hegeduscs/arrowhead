package eu.arrowhead.common.messages;

public class ConnectToConsumerResponse {

  private int serverSocketPort;

  public ConnectToConsumerResponse() {
  }

  public ConnectToConsumerResponse(int serverSocketPort) {
    this.serverSocketPort = serverSocketPort;
  }

  public int getServerSocketPort() {
    return serverSocketPort;
  }

  public void setServerSocketPort(int serverSocketPort) {
    this.serverSocketPort = serverSocketPort;
  }

}
