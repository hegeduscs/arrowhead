package eu.arrowhead.core.gateway;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import eu.arrowhead.common.messages.ConnectToProviderRequest;
import eu.arrowhead.common.security.SecurityUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
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
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
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
	 * @param brokerName
	 *            The name of the AMQP broker to use for connections
	 * @param brokerPort
	 *            The port of the AMQP broker to use for connections
	 * @param queueName
	 *            The name of the queue, should be unique
	 * @param controlQueueName
	 *            The name of the queue for control messages, should be unique
	 *
	 * @return GatewaySession
	 */
	static GatewaySession createChannel(String brokerName, int brokerPort, String queueName, String controlQueueName) {
		GatewaySession gatewaySession = new GatewaySession();
		try {
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost(brokerName);
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
	 * @param brokerName
	 *            The name of the AMQP broker to use for connections
	 * @param brokerPort
	 *            The port of the AMQP broker to use for connections
	 * @param queueName
	 *            The name of the queue, should be unique
	 * @param controlQueueName
	 *            The name of the queue for control messages, should be unique
	 *
	 * @return channel
	 */
	static GatewaySession createSecureChannel(String brokerName, int brokerPort, String queueName,
			String controlQueueName) {
		// Get password and path from app.properties
		String keystorePass = GatewayMain.getProp().getProperty("ssl.keystorepass");
		String keystorePath = GatewayMain.getProp().getProperty("ssl.keystore");

		String truststorePass = GatewayMain.getProp().getProperty("ssl.truststorepass");
		String truststorePath = GatewayMain.getProp().getProperty("ssl.truststore");

		KeyStore ks = SecurityUtils.loadKeyStore(keystorePath, keystorePass);
		KeyStore tks = SecurityUtils.loadKeyStore(truststorePath, truststorePass);

		KeyManagerFactory kmf = null;
		TrustManagerFactory tmf = null;
		try {
			kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, keystorePass.toCharArray());
			tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(tks);
		} catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException e) {
			e.printStackTrace();
			log.error("GatewayService: Initializing the keyManagerFactory/trusManagerFactory failed: " + e.toString()
					+ " " + e.getMessage());
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
		factory.setHost(brokerName);
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

	static SSLContext createSSLContext() {
		String keystorePath = GatewayMain.getProp().getProperty("ssl.keystore");
		String keystorePass = GatewayMain.getProp().getProperty("ssl.keystorepass");

		SSLContext sslContext = null;
		KeyManagerFactory kmf = null;

		KeyStore keyStore = SecurityUtils.loadKeyStore(keystorePath, keystorePass);

		try {
			kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(keyStore, keystorePass.toCharArray());
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), SecurityUtils.createTrustManagers(), null);
		} catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException | KeyManagementException e) {
			e.printStackTrace();
			log.error("GatewayService: Initializing the keyManagerFactory/trusManagerFactory failed");
		}
		return sslContext;
	}

	static void communicateWithProviderSecure(GatewaySession gatewaySession, String queueName, String controlQueueName,
			ConnectToProviderRequest connectionRequest) throws IOException {
		Channel channel = gatewaySession.getChannel();
		SSLSocket sslProviderSocket = null;
		GetResponse controlMessage = channel.basicGet(controlQueueName, false);
		while (controlMessage == null || !(new String(controlMessage.getBody()).equals("close"))) {
			GetResponse message = channel.basicGet(queueName, false);
			if (message == null) {
				System.out.println("No message retrieved");
			} else {
				SSLContext sslContext = GatewayService.createSSLContext();
				SSLSocketFactory clientFactory = sslContext.getSocketFactory();
				sslProviderSocket = (SSLSocket) clientFactory.createSocket(connectionRequest.getProvider().getAddress(),
						connectionRequest.getProvider().getPort());
				InputStream inProvider = sslProviderSocket.getInputStream();
				OutputStream outProvider = sslProviderSocket.getOutputStream();
				outProvider.write(message.getBody());

				// get the answer from Provider
				byte[] inputFromProvider = new byte[1024];
				byte[] inputFromProviderFinal = new byte[inProvider.read(inputFromProvider)];
				for (int i = 0; i < inputFromProviderFinal.length; i++) {
					inputFromProviderFinal[i] = inputFromProvider[i];
				}
				channel.basicPublish("", queueName, null, inputFromProviderFinal);
			}
			controlMessage = channel.basicGet(controlQueueName, false);
		}
		// Close sockets and the connection
		channel.close();
		gatewaySession.getConnection().close();
		sslProviderSocket.close();

	}

	static void communicateWithProviderInsecure(GatewaySession gatewaySession, String queueName,
			String controlQueueName, ConnectToProviderRequest connectionRequest) throws IOException {
		Channel channel = gatewaySession.getChannel();
		Socket providerSocket = null;
		GetResponse controlMessage = channel.basicGet(controlQueueName, false);
		while (controlMessage == null || !(new String(controlMessage.getBody()).equals("close"))) {
			GetResponse message = channel.basicGet(queueName, false);
			if (message == null) {
				System.out.println("No message retrieved");
			} else {
				providerSocket = new Socket(connectionRequest.getProvider().getAddress(),
						connectionRequest.getProvider().getPort());
				InputStream inProvider = providerSocket.getInputStream();
				OutputStream outProvider = providerSocket.getOutputStream();
				outProvider.write(message.getBody());

				// get the answer from Provider
				byte[] inputFromProvider = new byte[1024];
				byte[] inputFromProviderFinal = new byte[inProvider.read(inputFromProvider)];
				for (int i = 0; i < inputFromProviderFinal.length; i++) {
					inputFromProviderFinal[i] = inputFromProvider[i];
				}
				channel.basicPublish("", queueName, null, inputFromProviderFinal);
			}
			controlMessage = channel.basicGet(controlQueueName, false);
		}
		// Close sockets and the connection
		channel.close();
		gatewaySession.getConnection().close();
		providerSocket.close();
	}

	/*
	 * static Boolean checkRequester(SSLSession consumerSession,
	 * GatewayAtConsumerRequest connectionRequest) { String consumerCN =
	 * connectionRequest.getConsumer().getSystemName(); String consumerIP =
	 * connectionRequest.getConsumer().getAddress(); String consumerIPFromCert =
	 * consumerSession.getPeerHost();
	 * 
	 * Certificate[] servercerts = null; try { servercerts =
	 * consumerSession.getPeerCertificates(); } catch (SSLPeerUnverifiedException e)
	 * { e.printStackTrace(); } X509Certificate cert = (X509Certificate)
	 * servercerts[0]; String subjectname = cert.getSubjectDN().getName(); String
	 * consumerCNFromCert = SecurityUtils.getCertCNFromSubject(subjectname);
	 * 
	 * return (!consumerCN.equals(consumerCNFromCert) |
	 * !consumerIP.equals(consumerIPFromCert)); }
	 */

	/**
	 * Fill the ConcurrentHashMap with initial keys and values
	 *
	 * @param map
	 *            ConcurrentHashMap which contains the port number and the
	 *            availability
	 * @param portMin
	 *            The lowest port number from the allowed range
	 * @param portMax
	 *            The highest port number from the allowed range
	 *
	 * @return The initialized ConcurrentHashMap
	 */
	// Integer: port; Boolean: free (true) or reserved(false)
	static ConcurrentHashMap<Integer, Boolean> initPortAllocationMap(ConcurrentHashMap<Integer, Boolean> map,
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
	 * 
	 * @throws RuntimeException
	 */
	static Integer getAvailablePort() {
		Integer serverSocketPort = null;
		// Check the port range for
		ArrayList<Integer> freePorts = new ArrayList<Integer>();
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
