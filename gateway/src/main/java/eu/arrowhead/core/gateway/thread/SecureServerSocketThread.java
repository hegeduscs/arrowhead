package eu.arrowhead.core.gateway.thread;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import eu.arrowhead.common.messages.ConnectToConsumerRequest;
import eu.arrowhead.core.gateway.GatewayService;
import eu.arrowhead.core.gateway.model.GatewaySession;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import org.apache.log4j.Logger;

public class SecureServerSocketThread extends Thread {

  private int port;
  private SSLServerSocket sslServerSocket = null;
  private ConnectToConsumerRequest connectionRequest;
  private GatewaySession gatewaySession;
  private static final Logger log = Logger.getLogger(SecureServerSocketThread.class.getName());

  public SecureServerSocketThread(GatewaySession gatewaySession, int port, ConnectToConsumerRequest connectionRequest) {
    this.port = port;
    this.connectionRequest = connectionRequest;
    this.gatewaySession = gatewaySession;
  }

  // TODO narrower try-catches
  public void run() {
    SSLContext sslContext = GatewayService.createSSLContext();
    // Socket for server to listen at.
    SSLServerSocketFactory serverFactory = sslContext.getServerSocketFactory();
    try {
      sslServerSocket = (SSLServerSocket) serverFactory.createServerSocket(port);
      sslServerSocket.setNeedClientAuth(true);
      sslServerSocket.setSoTimeout(connectionRequest.getTimeout());
      System.out.println("Secure serverSocket is now running at port: " + port + "\n");
    } catch (IOException e) {
      e.printStackTrace();
      log.error("Creating secure serverSocket failed.");
    }

    try {
      // Accept a client connection once Server receives one.
      SSLSocket sslConsumerSocket = (SSLSocket) sslServerSocket.accept();
      SSLSession consumerSession = sslConsumerSocket.getSession();
      Channel channel = gatewaySession.getChannel();

      try {
        InputStream inConsumer = sslConsumerSocket.getInputStream();
        OutputStream outConsumer = sslConsumerSocket.getOutputStream();

        // Get the request from the Consumer
        byte[] inputFromConsumer = new byte[1024];
        byte[] inputFromConsumerFinal = new byte[inConsumer.read(inputFromConsumer)];
        System.arraycopy(inputFromConsumer, 0, inputFromConsumerFinal, 0, inputFromConsumerFinal.length);

        channel.basicPublish("", connectionRequest.getQueueName(), null, inputFromConsumerFinal);

        Consumer consumer = new DefaultConsumer(channel) {
          @Override
          public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
            outConsumer.write(body);
            System.out.println("Broker response: ");
            System.out.println(new String(body));
          }

        };

        channel.basicConsume(connectionRequest.getQueueName().concat("_resp"), true, consumer);

        Consumer controlConsumer = new DefaultConsumer(channel) {
          @Override
          public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
            if (new String(body).equals("close")) {
              GatewayService.makeServerSocketFree(port);
              // Close sockets and the connection
              try {
                channel.close();
                gatewaySession.getConnection().close();
              } catch (AlreadyClosedException e) {
                log.info("Channel already closed by Broker");
              }
              sslConsumerSocket.close();
              sslServerSocket.close();
              log.info("ConsumerSocket closed");
            }
          }
        };
        channel.basicConsume(connectionRequest.getControlQueueName().concat("_resp"), true, controlConsumer);

      } catch (SocketException e) {
        log.error("Socket closed by remote partner");
        GatewayService.makeServerSocketFree(port);
        // Close sockets and the connection
        try {
          channel.close();
          gatewaySession.getConnection().close();
        } catch (AlreadyClosedException error) {
          log.info("Channel already closed by Broker");
        }
        sslConsumerSocket.close();
        sslServerSocket.close();
        log.info("ConsumerSocket closed");
      }
    } catch (IOException e) {
      e.printStackTrace();
      log.error("SecureServerThread: Creating secure clientSocket failed.");
    }
  }

}
