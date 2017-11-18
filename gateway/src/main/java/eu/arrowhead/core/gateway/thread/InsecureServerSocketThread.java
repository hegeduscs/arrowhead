package eu.arrowhead.core.gateway.thread;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import eu.arrowhead.common.messages.ConnectToConsumerRequest;
import eu.arrowhead.core.gateway.GatewayService;
import eu.arrowhead.core.gateway.model.GatewaySession;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import org.apache.log4j.Logger;

public class InsecureServerSocketThread extends Thread {

  private int port;
  private ServerSocket serverSocket;
  private ConnectToConsumerRequest connectionRequest;
  private static final Logger log = Logger.getLogger(InsecureServerSocketThread.class.getName());

  public InsecureServerSocketThread(int port, ConnectToConsumerRequest connectionRequest) {
    this.port = port;
    this.connectionRequest = connectionRequest;
  }

  //TODO narrower try-catches + maybe create 1 (or 2 with secure version) method for the while loop part which has 4 different copies in 4 methods
  public void run() {

    try {
      serverSocket = new ServerSocket(port);
      System.out.println("Insecure serverSocket is now running at port: " + port);
    } catch (IOException e) {
      e.printStackTrace();
      log.error("Creating insecure ServerSocket failed");
    }

    try {
      // Create socket for Consumer
      Socket consumerSocket = serverSocket.accept();
      InputStream inConsumer = consumerSocket.getInputStream();
      OutputStream outConsumer = consumerSocket.getOutputStream();

      // Get the request from the Consumer
      byte[] inputFromConsumer = new byte[1024];
      byte[] inputFromConsumerFinal = new byte[inConsumer.read(inputFromConsumer)];
      System.arraycopy(inputFromConsumer, 0, inputFromConsumerFinal, 0, inputFromConsumerFinal.length);

      // Create a channel
      GatewaySession gatewaySession = GatewayService
          .createInsecureChannel(connectionRequest.getBrokerName(), connectionRequest.getBrokerPort(), connectionRequest.getQueueName(),
                                 connectionRequest.getControlQueueName());
      Channel channel = gatewaySession.getChannel();

      channel.basicPublish("", connectionRequest.getQueueName(), null, inputFromConsumerFinal);

      // Get the response and the control messages
      GetResponse controlMessage = channel.basicGet(connectionRequest.getControlQueueName(), false);
      while (controlMessage == null || !(new String(controlMessage.getBody()).equals("close"))) {
        GetResponse message = channel.basicGet(connectionRequest.getQueueName(), false);
        if (message == null) {
          System.out.println("No message retrieved");
        } else {
          outConsumer.write(message.getBody());
        }
        controlMessage = channel.basicGet(connectionRequest.getControlQueueName(), false);
      }

      // Close sockets and the connection
      channel.close();
      gatewaySession.getConnection().close();
      consumerSocket.close();
      serverSocket.close();

    } catch (IOException e) {
      e.printStackTrace();
      log.error("Creating insecure clientSocket failed");
    }
  }

}
