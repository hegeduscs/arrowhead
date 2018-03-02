/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.gateway;

import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.messages.ActiveSession;
import eu.arrowhead.common.messages.ConnectToConsumerRequest;
import eu.arrowhead.common.messages.ConnectToConsumerResponse;
import eu.arrowhead.common.messages.ConnectToProviderRequest;
import eu.arrowhead.common.messages.ConnectToProviderResponse;
import eu.arrowhead.common.messages.GatewayEncryption;
import eu.arrowhead.common.messages.GatewaySession;
import eu.arrowhead.common.security.SecurityUtils;
import eu.arrowhead.core.ArrowheadMain;
import eu.arrowhead.core.gateway.thread.InsecureServerSocketThread;
import eu.arrowhead.core.gateway.thread.InsecureSocketThread;
import eu.arrowhead.core.gateway.thread.SecureServerSocketThread;
import eu.arrowhead.core.gateway.thread.SecureSocketThread;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Map.Entry;
import java.util.ServiceConfigurationError;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import org.apache.log4j.Logger;

/**
 * Contains miscellaneous helper functions for the Gateway.
 */

public final class GatewayService {

  public static final String GATEWAY_PUBLIC_KEY;

  static final ConcurrentHashMap<String, ActiveSession> activeSessions = new ConcurrentHashMap<>();

  private static final Logger log = Logger.getLogger(GatewayService.class.getName());
  private static final ConcurrentHashMap<Integer, Boolean> portAllocationMap;
  private static final SSLContext cloudContext;
  private static final KeyStore gatewayKeyStore;
  private static final int ivSize = 16;
  private static final int keySize = 16;
  private static final int minPort;
  private static final int maxPort;

  private GatewayService() throws AssertionError {
    throw new AssertionError("GatewayService is a non-instantiable class");
  }

  static {
    minPort = Integer.parseInt(ArrowheadMain.getProp().getProperty("min_port"));
    maxPort = Integer.parseInt(ArrowheadMain.getProp().getProperty("max_port"));
    portAllocationMap = GatewayService.initPortAllocationMap(new ConcurrentHashMap<>(), minPort, maxPort);

    String cloudKeystorePath = ArrowheadMain.getProp().getProperty("cloud_keystore");
    String cloudKeystorePass = ArrowheadMain.getProp().getProperty("cloud_keystore_pass");
    String cloudKeyPass = ArrowheadMain.getProp().getProperty("cloud_keypass");
    String masterArrowheadCertPath = ArrowheadMain.getProp().getProperty("master_arrowhead_cert");
    cloudContext = SecurityUtils.createMasterSSLContext(cloudKeystorePath, cloudKeystorePass, cloudKeyPass, masterArrowheadCertPath);

    String gatewayKeystorePath = ArrowheadMain.getProp().getProperty("gateway_keystore");
    String gatewayKeystorePass = ArrowheadMain.getProp().getProperty("gateway_keystore_pass");
    gatewayKeyStore = SecurityUtils.loadKeyStore(gatewayKeystorePath, gatewayKeystorePass);
    X509Certificate serverCert = SecurityUtils.getFirstCertFromKeyStore(gatewayKeyStore);
    GATEWAY_PUBLIC_KEY = Base64.getEncoder().encodeToString(serverCert.getPublicKey().getEncoded());
  }

  public static ConnectToProviderResponse connectToProvider(ConnectToProviderRequest connectionRequest) {
    String queueName = String.valueOf(System.currentTimeMillis()).concat(String.valueOf(Math.random())).replace(".", "");
    String controlQueueName = queueName.concat("_control");

    ActiveSession activeSession = new ActiveSession(connectionRequest.getConsumer(), connectionRequest.getConsumerCloud(),
                                                    connectionRequest.getProvider(), connectionRequest.getProviderCloud(),
                                                    connectionRequest.getService(), connectionRequest.getBrokerName(),
                                                    connectionRequest.getBrokerPort(), null, queueName, controlQueueName,
                                                    connectionRequest.getIsSecure(), new Date(System.currentTimeMillis()));
    // Add the session to the management queue
    GatewayService.activeSessions.put(queueName, activeSession);

    GatewaySession gatewaySession = GatewayService
        .createChannel(connectionRequest.getBrokerName(), connectionRequest.getBrokerPort(), queueName, controlQueueName,
                       connectionRequest.getIsSecure());

    if (connectionRequest.getIsSecure()) {
      SecureSocketThread secureThread = new SecureSocketThread(gatewaySession, queueName, controlQueueName, connectionRequest);
      secureThread.start();
    } else {
      InsecureSocketThread insecureThread = new InsecureSocketThread(gatewaySession, queueName, controlQueueName, connectionRequest);
      insecureThread.start();
    }

    log.info("Returning the ConnectToProviderResponse to the Gatekeeper");
    return new ConnectToProviderResponse(queueName, controlQueueName);
  }

  public static ConnectToConsumerResponse connectToConsumer(ConnectToConsumerRequest connectionRequest) {
    Integer serverSocketPort = GatewayService.getAvailablePort();

    ActiveSession activeSession = new ActiveSession(connectionRequest.getConsumer(), connectionRequest.getConsumerCloud(),
                                                    connectionRequest.getProvider(), connectionRequest.getProviderCloud(),
                                                    connectionRequest.getService(), connectionRequest.getBrokerName(),
                                                    connectionRequest.getBrokerPort(), serverSocketPort, connectionRequest.getQueueName(),
                                                    connectionRequest.getControlQueueName(), connectionRequest.getIsSecure(),
                                                    new Date(System.currentTimeMillis()));
    // Add the session to the management queue
    GatewayService.activeSessions.put(connectionRequest.getQueueName(), activeSession);

    GatewaySession gatewaySession = GatewayService
        .createChannel(connectionRequest.getBrokerName(), connectionRequest.getBrokerPort(), connectionRequest.getQueueName(),
                       connectionRequest.getControlQueueName(), connectionRequest.getIsSecure());

    if (connectionRequest.getIsSecure()) {
      SecureServerSocketThread secureThread = new SecureServerSocketThread(gatewaySession, serverSocketPort, connectionRequest);
      secureThread.start();
    } else {
      InsecureServerSocketThread insecureThread = new InsecureServerSocketThread(gatewaySession, serverSocketPort, connectionRequest);
      insecureThread.start();
    }

    log.info("Returning the ConnectToConsumerResponse to the Gatekeeper");
    return new ConnectToConsumerResponse(serverSocketPort);
  }

  /**
   * Creates a channel to the Broker
   *
   * @param brokerHost The hostname of the AMQP broker to use for connections
   * @param brokerPort The port of the AMQP broker to use for connections
   * @param queueName The name of the queue, should be unique
   * @param controlQueueName The name of the queue for control messages, should be unique
   * @param isSecure The type of the channel (secure or insecure)
   *
   * @return GatewaySession, which contains a connection and a channel object
   */
  private static GatewaySession createChannel(String brokerHost, int brokerPort, String queueName, String controlQueueName, boolean isSecure) {
    GatewaySession gatewaySession = new GatewaySession();
    try {
      ConnectionFactory factory = new ConnectionFactory();
      factory.setHost(brokerHost);
      factory.setPort(brokerPort);

      if (isSecure) {
        try {
          factory.useSslProtocol(cloudContext);
        } catch (RuntimeException e) {
          throw new ArrowheadException("Gateway is in insecure mode, and can not create a secure channel with the AMQP broker!", 500,
                                       "GatewayService:createChannel", e);
        }
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
      throw new ArrowheadException(e.getClass().getName() + ": " + e.getMessage(), 500, "GatewayService:createChannel", e);
    }

    return gatewaySession;
  }

  public static GatewayEncryption encryptMessage(byte[] message, String publicKeyString) {
    Cipher cipherRSA;
    Cipher cipherAES;
    PublicKey publicKey;
    byte[] encryptedAESKey;
    byte[] encryptedIVAndMessage;

    try {
      publicKey = SecurityUtils.getPublicKey(publicKeyString);
      cipherRSA = Cipher.getInstance("RSA/ECB/PKCS1Padding");
      cipherRSA.init(Cipher.ENCRYPT_MODE, publicKey);
    } catch (GeneralSecurityException e) {
      log.fatal("The initialization of the RSA cipher failed.");
      throw new ArrowheadException(e.getClass().getName() + ": " + e.getMessage(), 500, "GatewayService:encryptMessage", e);
    }

    // Creating the random IV (Initialization vector)
    SecureRandom random = new SecureRandom();
    byte[] IV = new byte[ivSize];
    random.nextBytes(IV);
    IvParameterSpec ivParameterSpec = new IvParameterSpec(IV);

    // Creating the random AES (Advanced Encryption Standard) key
    byte[] SECRET_KEY = new byte[keySize];
    random.nextBytes(SECRET_KEY);
    SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY, "AES");

    try {
      encryptedAESKey = cipherRSA.doFinal(secretKeySpec.getEncoded());
      cipherAES = Cipher.getInstance("AES/CBC/PKCS5PADDING");
      cipherAES.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
      byte[] encryptedMessage = cipherAES.doFinal(message);

      // Combine IV and encrypted part.
      encryptedIVAndMessage = new byte[ivSize + encryptedMessage.length];
      System.arraycopy(IV, 0, encryptedIVAndMessage, 0, ivSize);
      System.arraycopy(encryptedMessage, 0, encryptedIVAndMessage, ivSize, encryptedMessage.length);

      // Initialize and return the value
      return new GatewayEncryption(encryptedAESKey, encryptedIVAndMessage);

    } catch (GeneralSecurityException e) {
      log.fatal("Something goes wrong while AES encryption.");
      throw new ArrowheadException(e.getClass().getName() + ": " + e.getMessage(), 500, "GatewayService:encryptMessage", e);
    }
  }

  public static byte[] decryptMessage(GatewayEncryption gatewayEncryption) {
    Cipher cipherRSA;
    Cipher cipherAES;
    PrivateKey privateKey;
    byte[] decryptedMessage;

    try {
      privateKey = SecurityUtils.getPrivateKey(gatewayKeyStore, ArrowheadMain.getProp().getProperty("gateway_keystore_pass"));
      cipherRSA = Cipher.getInstance("RSA/ECB/PKCS1Padding");
      cipherRSA.init(Cipher.DECRYPT_MODE, privateKey);
    } catch (GeneralSecurityException e) {
      log.fatal("The initialization of the RSA cipher failed.");
      throw new ArrowheadException(e.getClass().getName() + ": " + e.getMessage(), 500, "GatewayService:decryptMessage", e);
    }

    try {
      byte[] decryptedAESKey = cipherRSA.doFinal(gatewayEncryption.getEncryptedAESKey());

      // Extract IV.
      byte[] IV_DECRYPT = new byte[ivSize];
      System.arraycopy(gatewayEncryption.getEncryptedIVAndMessage(), 0, IV_DECRYPT, 0, IV_DECRYPT.length);
      IvParameterSpec ivParameterSpec = new IvParameterSpec(IV_DECRYPT);

      // Extract encrypted part.
      int encryptedSize = gatewayEncryption.getEncryptedIVAndMessage().length - ivSize;
      byte[] encryptedBytes = new byte[encryptedSize];
      System.arraycopy(gatewayEncryption.getEncryptedIVAndMessage(), ivSize, encryptedBytes, 0, encryptedSize);

      SecretKeySpec secretKeySpec = new SecretKeySpec(decryptedAESKey, "AES");
      cipherAES = Cipher.getInstance("AES/CBC/PKCS5PADDING");
      cipherAES.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
      decryptedMessage = cipherAES.doFinal(encryptedBytes);
    } catch (GeneralSecurityException e) {
      log.fatal("Something goes wrong while AES decryption.");
      throw new ArrowheadException(e.getClass().getName() + ": " + e.getMessage(), 500, "GatewayService:decryptMessage", e);
    }

    return decryptedMessage;
  }

  public static SSLContext createSSLContext() {
    SSLContext sslContext;
    KeyManagerFactory kmf;
    try {
      kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      kmf.init(gatewayKeyStore, ArrowheadMain.getProp().getProperty("gateway_keystore_pass").toCharArray());
      sslContext = SSLContext.getInstance("TLS");
      sslContext.init(kmf.getKeyManagers(), SecurityUtils.createTrustManagers(), null);

    } catch (NoSuchAlgorithmException | KeyManagementException e) {
      log.fatal("createSSLContext: Initializing the keyManagerFactory failed");
      throw new ServiceConfigurationError("Initializing the keyManagerFactory failed for the SSLContext failed", e);

    } catch (KeyStoreException | UnrecoverableKeyException e) {
      log.error("createSSLContext: keystore malformed, factory init failed");
      throw new AuthException("Keystore is malformed, or the password is invalid", e);
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
  private static ConcurrentHashMap<Integer, Boolean> initPortAllocationMap(ConcurrentHashMap<Integer, Boolean> map, int portMin, int portMax) {
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
  private static Integer getAvailablePort() {
    Integer serverSocketPort;
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

  public static void consumerSideClose(GatewaySession gatewaySession, Integer port, Socket consumerSocket, ServerSocket serverSocket,
                                       String queueName) {
    // Setting serverSocket free
    portAllocationMap.put(port, true);
    activeSessions.remove(queueName);
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

  public static void providerSideClose(GatewaySession gatewaySession, Socket providerSocket, String queueName) {
    try {
      activeSessions.remove(queueName);
      if (providerSocket != null) {
        providerSocket.close();
      }
      gatewaySession.getChannel().close();
      gatewaySession.getConnection().close();
      log.info("ProviderSocket closed");
    } catch (AlreadyClosedException | IOException e) {
      log.info("Channel already closed");
    }
  }

}
