package eu.arrowhead.core.gateway;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ServiceConfigurationError;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.log4j.Logger;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import eu.arrowhead.common.messages.GatewayAtConsumerResponse;
import eu.arrowhead.common.security.SecurityUtils;

/**
 * Contains miscellaneous helper functions for the Gateway.
 */
// TODO channel close issue in all function, maybe static variable in GWMain?
public class GatewayService {

	private static Logger log = Logger.getLogger(GatewayService.class.getName());

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
	 *
	 * @return channel
	 */
	static Channel createChannel(String brokerName, int brokerPort, String queueName) {
		Channel channel = null;
		try {
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost(brokerName);
			factory.setPort(brokerPort);
			Connection connection = factory.newConnection();
			channel = connection.createChannel();
			channel.queueDeclare(queueName, false, false, false, null);
		} catch (IOException e) {
			e.printStackTrace();
			log.info("GatewayService: Creating the insecure channel failed");
		}
		return channel;
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
	 *
	 * @return channel
	 */
	static Channel createSecureChannel(String brokerName, int brokerPort, String queueName) {
		String keyPass = "12345";
		String trustPass = "12345";
		String keyFilePath = "C:\\Users\\sga\\Downloads\\arrowhead\\certificates\\testcloud1\\client1\\client1.testcloud1.jks";
		String trustFilePath = "C:\\Users\\sga\\Downloads\\arrowhead\\certificates\\testcloud1\\testcloud1_cert.jks";
		KeyStore ks = SecurityUtils.loadKeyStore(keyFilePath, keyPass);
		KeyStore tks = SecurityUtils.loadKeyStore(trustFilePath, trustPass);

		KeyManagerFactory kmf = null;
		TrustManagerFactory tmf = null;
		try {
			kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, keyPass.toCharArray());
			tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(tks);
		} catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException e) {
			e.printStackTrace();
			log.fatal("GatewayService: Initializing the keyManagerFactory/trusManagerFactory failed: " + e.toString()
					+ " " + e.getMessage());
			throw new ServiceConfigurationError("Initializing the keyManagerFactory/trusManagerFactory failed...", e);
		}

		SSLContext c = null;
		try {
			c = SSLContext.getInstance("TLSv1.1");
			c.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			e.printStackTrace();
			log.info("GatewayService: Initializing the sslcontext failed");
		}

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(brokerName);
		factory.setPort(brokerPort); // secure port: 5671
		factory.useSslProtocol(c);

		Channel channel = null;
		try {
			Connection conn = factory.newConnection();
			channel = conn.createChannel();
			channel.queueDeclare(queueName, false, true, true, null);
		} catch (IOException e) {
			e.printStackTrace();
			log.info("GatewayService: Creating the secure channel failed");
		}

		return channel;
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
			log.fatal("GatewayService: Initializing the keyManagerFactory/trusManagerFactory failed");
		}
		return sslContext;
	}

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
		for (Entry<Integer, Boolean> entry : GatewayMain.getPortAllocationMap().entrySet()) {
			if (entry.getValue().equals(true)) {
				freePorts.add(entry.getKey());
			}
		}

		if (freePorts.isEmpty()) {
			log.fatal("No available port found in port range");
			throw new RuntimeException("No available port found in port range");
		} else {
			serverSocketPort = freePorts.get(0);
		}
		return serverSocketPort;
	}

}
