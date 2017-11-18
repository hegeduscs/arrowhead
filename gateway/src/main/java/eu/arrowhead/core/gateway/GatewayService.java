package eu.arrowhead.core.gateway;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import eu.arrowhead.common.security.SecurityUtils;
import eu.arrowhead.core.gateway.model.GatewaySession;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.ServiceConfigurationError;
import java.util.concurrent.ConcurrentHashMap;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.apache.log4j.Logger;

/**
 * Contains miscellaneous helper functions for the Gateway.
 */

public class GatewayService {

  private static final Logger log = Logger.getLogger(GatewayService.class.getName());

  private GatewayService() throws AssertionError {
    throw new AssertionError("GatewayService is a non-instantiable class");
  }

  /**
   * Creates an insecure channel
   *
   * @param brokerHost The hostname of the AMQP broker to use for connections
   * @param brokerPort The port of the AMQP broker to use for connections
   * @param queueName The name of the queue, should be unique
   * @param controlQueueName The name of the queue for control messages, should be unique
   *
   * @return GatewaySession
   */
  public static GatewaySession createInsecureChannel(String brokerHost, int brokerPort, String queueName, String controlQueueName) {
    GatewaySession gatewaySession = new GatewaySession();
    try {
      ConnectionFactory factory = new ConnectionFactory();
      factory.setHost(brokerHost);
      factory.setPort(brokerPort);
      Connection connection = factory.newConnection();
      Channel channel = connection.createChannel();
      channel.queueDeclare(queueName, false, false, false, null);
      channel.queueDeclare(controlQueueName, false, false, false, null);
      gatewaySession.setConnection(connection);
      gatewaySession.setChannel(channel);

    } catch (IOException e) {
      e.printStackTrace();
      log.error("GatewayService: Creating the insecure channel failed");
    }
    return gatewaySession;
  }

  /**
   * Creates a secure channel
   *
   * @param brokerHost The hostname of the AMQP broker to use for connections
   * @param brokerPort The port of the AMQP broker to use for connections
   * @param queueName The name of the queue, should be unique
   * @param controlQueueName The name of the queue for control messages, should be unique
   *
   * @return channel
   */
  public static GatewaySession createSecureChannel(String brokerHost, int brokerPort, String queueName, String controlQueueName) {
    // Get keystore and truststore files from app.properties
    String keystorePass = GatewayMain.getProp().getProperty("keystorepass");
    String keystorePath = GatewayMain.getProp().getProperty("keystore");
    String truststorePass = GatewayMain.getProp().getProperty("truststorepass");
    String truststorePath = GatewayMain.getProp().getProperty("truststore");

    KeyStore ks = SecurityUtils.loadKeyStore(keystorePath, keystorePass);
    KeyStore tks = SecurityUtils.loadKeyStore(truststorePath, truststorePass);

    KeyManagerFactory kmf;
    TrustManagerFactory tmf;
    try {
      kmf = KeyManagerFactory.getInstance("SunX509");
      kmf.init(ks, keystorePass.toCharArray());
      tmf = TrustManagerFactory.getInstance("SunX509");
      tmf.init(tks);
    } catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException e) {
      e.printStackTrace();
      log.fatal("GatewayService: Initializing the keyManagerFactory/trusManagerFactory failed: " + e.toString() + " " + e.getMessage());
      throw new ServiceConfigurationError("Initializing the keyManagerFactory/trusManagerFactory failed", e);
    }

    SSLContext c = null;
    try {
      c = SSLContext.getInstance("TLSv1.1");
      c.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
    } catch (NoSuchAlgorithmException | KeyManagementException e) {
      e.printStackTrace();
      log.error("GatewayService: Initializing the sslcontext failed");
    }

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(brokerHost);
    factory.setPort(brokerPort); // secure port: 5671
    factory.useSslProtocol(c);

    GatewaySession gatewaySession = new GatewaySession();
    try {
      Connection connection = factory.newConnection();
      Channel channel = connection.createChannel();
      channel.queueDeclare(queueName, false, true, true, null);
      channel.queueDeclare(controlQueueName, false, false, false, null);
      gatewaySession.setConnection(connection);
      gatewaySession.setChannel(channel);
    } catch (IOException e) {
      e.printStackTrace();
      log.error("GatewayService: Creating the secure channel failed");
    }

    return gatewaySession;
  }

  public static SSLContext createSSLContext() {
    String keystorePath = GatewayMain.getProp().getProperty("keystore");
    String keystorePass = GatewayMain.getProp().getProperty("keystorepass");
    KeyStore keyStore = SecurityUtils.loadKeyStore(keystorePath, keystorePass);

    SSLContext sslContext = null;
    KeyManagerFactory kmf = null;
    try {
      kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      kmf.init(keyStore, keystorePass.toCharArray());
      sslContext = SSLContext.getInstance("TLS");
      sslContext.init(kmf.getKeyManagers(), SecurityUtils.createTrustManagers(), null);
    } catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException | KeyManagementException e) {
      e.printStackTrace();
      log.error("createSSLContext: Initializing the keyManagerFactory failed");
    }
    return sslContext;
  }

  /**
   * Fill the ConcurrentHashMap with initial keys and values
   *
   * @param map ConcurrentHashMap which contains the port number and the availability
   * @param portMin The lowest port number from the allowed range
   * @param portMax The highest port number from the allowed range
   *
   * @return The initialized ConcurrentHashMap
   */
  // Integer: port; Boolean: free (true) or reserved(false)
  static ConcurrentHashMap<Integer, Boolean> initPortAllocationMap(ConcurrentHashMap<Integer, Boolean> map, int portMin, int portMax) {
    for (int i = portMin; i <= portMax; i++) {
      map.put(i, true);
    }
    return map;
  }

  /**
   * Search for an available port in the port range
   *
   * @return serverSocketPort or null if no available port found
   */
  static Integer getAvailablePort() {
    Integer serverSocketPort = null;
    // Check the port range for
    ArrayList<Integer> freePorts = new ArrayList<>();
    for (Entry<Integer, Boolean> entry : GatewayMain.portAllocationMap.entrySet()) {
      if (entry.getValue().equals(true)) {
        freePorts.add(entry.getKey());
      }
    }

    if (freePorts.isEmpty()) {
      log.error("No available port found in port range");
      throw new RuntimeException("No available port found in port range");
    } else {
      serverSocketPort = freePorts.get(0);
    }
    return serverSocketPort;
  }

}
