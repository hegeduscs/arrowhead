package eu.arrowhead.core.gateway.thread;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import eu.arrowhead.common.messages.ConnectToProviderRequest;
import eu.arrowhead.core.gateway.GatewayService;
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
  private ConnectToProviderRequest connectionRequest;
  private static final Logger log = Logger.getLogger(SecureSocketThread.class.getName());

  public SecureSocketThread(GatewaySession gatewaySession, String queueName, String controlQueueName, ConnectToProviderRequest connectionRequest) {
    this.gatewaySession = gatewaySession;
    this.queueName = queueName;
    this.controlQueueName = controlQueueName;
    this.connectionRequest = connectionRequest;
  }

  public void run() {
    try {
      Channel channel = gatewaySession.getChannel();
      SSLContext sslContext = GatewayService.createSSLContext();
      SSLSocketFactory clientFactory = sslContext.getSocketFactory();
      SSLSocket sslProviderSocket = null;
      GetResponse controlMessage = channel.basicGet(controlQueueName, false);
      while (controlMessage == null || !(new String(controlMessage.getBody()).equals("close"))) {
        GetResponse message = channel.basicGet(queueName, false);
        if (message == null) {
          System.out.println("No message retrieved");
        } else {
          sslProviderSocket = (SSLSocket) clientFactory
              .createSocket(connectionRequest.getProvider().getAddress(), connectionRequest.getProvider().getPort());
          InputStream inProvider = sslProviderSocket.getInputStream();
          OutputStream outProvider = sslProviderSocket.getOutputStream();
          outProvider.write(message.getBody());

          // get the answer from Provider
          byte[] inputFromProvider = new byte[1024];
          byte[] inputFromProviderFinal = new byte[inProvider.read(inputFromProvider)];
          System.arraycopy(inputFromProvider, 0, inputFromProviderFinal, 0, inputFromProviderFinal.length);
          channel.basicPublish("", queueName, null, inputFromProviderFinal);
        }
        controlMessage = channel.basicGet(controlQueueName, false);
      }
      // Close sockets and the connection
      channel.close();
      gatewaySession.getConnection().close();
      if (sslProviderSocket != null) {
        sslProviderSocket.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
      log.error("ConnectToProvider(secure): I/O exception occured");
    }
  }

}
