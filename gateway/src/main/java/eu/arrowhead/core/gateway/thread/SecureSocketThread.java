package eu.arrowhead.core.gateway.thread;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.messages.ConnectToProviderRequest;
import eu.arrowhead.core.gateway.GatewayService;
import eu.arrowhead.core.gateway.model.GatewayEncryption;
import eu.arrowhead.core.gateway.model.GatewaySession;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.apache.log4j.Logger;

public class SecureSocketThread extends Thread {

  private GatewaySession gatewaySession;
  private String queueName;
  private String controlQueueName;
  private SSLSocket sslProviderSocket;
  private ConnectToProviderRequest connectionRequest;
  private GatewayEncryption gatewayEncryption;

  private static Boolean isAesKey = true;
  private static final Logger log = Logger.getLogger(SecureSocketThread.class.getName());

  public SecureSocketThread(GatewaySession gatewaySession, String queueName, String controlQueueName,
      ConnectToProviderRequest connectionRequest) {
    this.gatewaySession = gatewaySession;
    this.queueName = queueName;
    this.controlQueueName = controlQueueName;
    this.connectionRequest = connectionRequest;
  }

  public void run() {
    try {
      // Creating SSLsocket for Provider
      Channel channel = gatewaySession.getChannel();
      SSLContext sslContext = GatewayService.createSSLContext();
      SSLSocketFactory clientFactory = sslContext.getSocketFactory();
      SSLSocket sslProviderSocket = (SSLSocket) clientFactory.createSocket(connectionRequest.getProvider().getAddress(),
          connectionRequest.getProvider().getPort());
      sslProviderSocket.setSoTimeout(connectionRequest.getTimeout());
      InputStream inProvider = sslProviderSocket.getInputStream();
      OutputStream outProvider = sslProviderSocket.getOutputStream();
      log.info("Create SSLsocket for Provider");

      // Receiving messages through AMQP Broker

      Consumer consumer = new DefaultConsumer(channel) {
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
            throws IOException {
          if (isAesKey) {
            isAesKey = false;
            gatewayEncryption.setEncryptedAESKey(body);
            System.out.println("AES Key received.");
          } else {
            isAesKey = true;
            gatewayEncryption.setEncryptedIVAndMessage(body);
            byte[] decryptedMessage = GatewayService.decryptMessage(gatewayEncryption);
            outProvider.write(decryptedMessage);
            log.info("Sending the request to Provider");

            // get the answer from Provider
            byte[] inputFromProvider = new byte[1024];
            byte[] inputFromProviderFinal = new byte[inProvider.read(inputFromProvider)];
            System.arraycopy(inputFromProvider, 0, inputFromProviderFinal, 0, inputFromProviderFinal.length);
            log.info("Sending the response to Consumer");
            GatewayEncryption response = GatewayService.encryptMessage(inputFromProviderFinal,
                connectionRequest.getConsumerGWPublicKey());
            channel.basicPublish("", queueName.concat("_resp"), null, response.getEncryptedAESKey());
            channel.basicPublish("", queueName.concat("_resp"), null, response.getEncryptedIVAndMessage());
            channel.basicPublish("", controlQueueName.concat("_resp"), null, "close".getBytes());
          }
        }
      };

      Consumer controlConsumer = new DefaultConsumer(channel) {
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
            byte[] body) {
          if (new String(body).equals("close")) {
            GatewayService.providerSideClose(gatewaySession, sslProviderSocket);
          }
        }
      };

      while (true) {
        channel.basicConsume(queueName, true, consumer);
        channel.basicConsume(queueName, true, consumer);
        channel.basicConsume(controlQueueName, true, controlConsumer);
      }

    } catch (IOException | NegativeArraySizeException e) {
      e.printStackTrace();
      log.error("ConnectToProvider(secure): I/O exception occured");
      GatewayService.providerSideClose(gatewaySession, sslProviderSocket);
      throw new ArrowheadException(e.getMessage(), e);

    }
  }

}
