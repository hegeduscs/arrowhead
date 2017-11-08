package eu.arrowhead.core.gateway.model;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class GatewaySession {

  private Connection connection;
  private Channel channel;

  public GatewaySession() {
  }

  public GatewaySession(Connection connection, Channel channel) {
    this.connection = connection;
    this.channel = channel;
  }

  public Connection getConnection() {
    return connection;
  }

  public void setConnection(Connection connection) {
    this.connection = connection;
  }

  public Channel getChannel() {
    return channel;
  }

  public void setChannel(com.rabbitmq.client.Channel channel2) {
    this.channel = channel2;
  }

}
