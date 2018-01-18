package eu.arrowhead.core.gateway;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.ServiceConfigurationError;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.apache.log4j.Logger;

import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthenticationException;
import eu.arrowhead.common.security.SecurityUtils;
import eu.arrowhead.core.gateway.model.GatewaySession;

/**
 * Contains miscellaneous helper functions for the Gateway.
 */

public class GatewayService {

  private static final Logger log = Logger.getLogger(GatewayService.class.getName());
  private static final int minPort = Integer.parseInt(GatewayMain.getProp().getProperty("min_port"));
  private static final int maxPort = Integer.parseInt(GatewayMain.getProp().getProperty("max_port"));
  private static ConcurrentHashMap<Integer, Boolean> portAllocationMap = GatewayService
      .initPortAllocationMap(new ConcurrentHashMap<Integer, Boolean>(), minPort, maxPort);

  private GatewayService() throws AssertionError {
    throw new AssertionError("GatewayService is a non-instantiable class");
  }

  /**
   * Creates a channel to the Broker
   *
   * @param brokerHost
   *          The hostname of the AMQP broker to use for connections
   * @param brokerPort
   *          The port of the AMQP broker to use for connections
   * @param queueName
   *          The name of the queue, should be unique
   * @param controlQueueName
   *          The name of the queue for control messages, should be unique
   * @param isSecure
   *          The type of the channel (secure or insecure)
   * @return GatewaySession, which contains a connection and a channel object
   */
  public static GatewaySession createChannel(String brokerHost, int brokerPort, String queueName,
      String controlQueueName, boolean isSecure) {
    GatewaySession gatewaySession = new GatewaySession();
    try {
      ConnectionFactory factory = new ConnectionFactory();
      factory.setHost(brokerHost);
      factory.setPort(brokerPort);

      if (isSecure) {
        factory.useSslProtocol(GatewayMain.sslContext);
      }
      Connection connection = factory.newConnection();
      Channel channel = connection.createChannel();
      channel.queueDeclare(queueName, false, false, false, null);
      channel.queueDeclare(queueName.concat("_resp"), false, false, false, null);
      channel.queueDeclare(controlQueueName, false, false, false, null);
      channel.queueDeclare(controlQueueName.concat("_resp"), false, false, false, null);
      gatewaySession.setConnection(connection);
      gatewaySession.setChannel(channel);

    } catch (IOException | NullPointerException e) {
      log.error("Creating the channel to the Broker failed");
      throw new RuntimeException(e.getMessage(), e);

    }
    return gatewaySession;
  }

  public static byte[] encryptMessage(byte[] message, String publicKeyString) {
    Cipher cipher = null;
    byte[] encryptedMessage = null;
    PublicKey publicKey;

    try {
      publicKey = SecurityUtils.getPublicKey(publicKeyString);
    } catch (InvalidKeySpecException e) {
      log.fatal("The public key of the Gateway module is invalid");
      throw new ArrowheadException(e.getMessage(), e);
    }
    // Getting the private key from the keystore
    /*
     * KeyStore keyStore =
     * SecurityUtils.loadKeyStore(GatewayMain.getProp().getProperty("keystore"),
     * GatewayMain.getProp().getProperty("keystorepass")); PrivateKey privateKey =
     * SecurityUtils.getPrivateKey(keyStore,
     * GatewayMain.getProp().getProperty("keystorepass"));
     */
    try {
      cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
      log.fatal("Cipher.getInstance(String) throws exception, code needs to be changed!");
    }

    // Generate the encrypted message
    try {
      cipher.init(Cipher.ENCRYPT_MODE, publicKey);
      encryptedMessage = cipher.doFinal(message);
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Cipher or Signature class throws public key specific exception: " + e.getMessage());
    }
    return encryptedMessage;

  }

  public static byte[] decryptMessage(byte[] encryptedMessage) {
    Cipher cipher = null;
    byte[] decryptedMessage = null;

    try {
      cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
      log.fatal("Cipher.getInstance(String) throws exception, code needs to be changed!");
    }
    try {
      KeyStore keyStore = SecurityUtils.loadKeyStore(GatewayMain.getProp().getProperty("keystore"),
          GatewayMain.getProp().getProperty("keystorepass"));
      PrivateKey privateKey = SecurityUtils.getPrivateKey(keyStore, GatewayMain.getProp().getProperty("keystorepass"));
      // decrypt the text using the private key
      cipher.init(Cipher.DECRYPT_MODE, privateKey);
      decryptedMessage = cipher.doFinal(encryptedMessage);

    } catch (Exception e) {
      e.printStackTrace();
      log.error("Cipher or Signature class throws public key specific exception: " + e.getMessage());
    }

    return decryptedMessage;
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
    } catch (NoSuchAlgorithmException | KeyManagementException e) {
      log.fatal("createSSLContext: Initializing the keyManagerFactory failed");
      throw new ServiceConfigurationError("Initializing the keyManagerFactory failed for the SSLContext failed", e);
    } catch (KeyStoreException | UnrecoverableKeyException e) {
      log.error("createSSLContext: keystore malformed, factory init failed");
      throw new AuthenticationException("Keystore is malformed, or the password is invalid", e);
    }
    return sslContext;
  }

  /**
   * Fill the ConcurrentHashMap with initial keys and values
   *
   * @param map
   *          ConcurrentHashMap which contains the port number and the
   *          availability
   * @param portMin
   *          The lowest port number from the allowed range
   * @param portMax
   *          The highest port number from the allowed range
   *
   * @return The initialized ConcurrentHashMap
   */
  // Integer: port; Boolean: free (true) or reserved(false)
  private static ConcurrentHashMap<Integer, Boolean> initPortAllocationMap(ConcurrentHashMap<Integer, Boolean> map,
      int portMin, int portMax) {
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
  public static Integer getAvailablePort() {
    Integer serverSocketPort = null;
    // Check the port range for
    ArrayList<Integer> freePorts = new ArrayList<>();
    for (Entry<Integer, Boolean> entry : portAllocationMap.entrySet()) {
      if (entry.getValue().equals(true)) {
        freePorts.add(entry.getKey());
      }
    }

    if (freePorts.isEmpty()) {
      log.error("No available port found in port range");
      throw new RuntimeException("No available port found in port range");
    } else {
      serverSocketPort = freePorts.get(0);
      portAllocationMap.put(serverSocketPort, false);
    }
    return serverSocketPort;
  }

  public static void consumerSideClose(GatewaySession gatewaySession, Integer port, Socket consumerSocket,
      ServerSocket serverSocket) {
    log.error("Socket closed by remote partner");
    // Setting serverSocket free
    portAllocationMap.put(port, true);
    try {
      gatewaySession.getChannel().close();
      gatewaySession.getConnection().close();
      if (consumerSocket != null) {
        consumerSocket.close();
      }
      serverSocket.close();
      log.info("ConsumerSocket closed");
    } catch (AlreadyClosedException | IOException e) {
      log.info("Channel already closed");
    }
  }

  public static void providerSideClose(GatewaySession gatewaySession, Socket providerSocket) {
    try {
      providerSocket.close();
      gatewaySession.getChannel().close();
      gatewaySession.getConnection().close();
      log.info("ProviderSocket closed");
    } catch (AlreadyClosedException | IOException e) {
      log.info("Channel already closed");
    }
  }

}
