package eu.arrowhead.core.gateway;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import eu.arrowhead.common.exception.AuthenticationException;
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
	 * Creates a channel to the Broker
	 *
	 * @param brokerHost
	 *            The hostname of the AMQP broker to use for connections
	 * @param brokerPort
	 *            The port of the AMQP broker to use for connections
	 * @param queueName
	 *            The name of the queue, should be unique
	 * @param controlQueueName
	 *            The name of the queue for control messages, should be unique
	 * @param isSecure
	 *            The type of the channel (secure or insecure)
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
			e.printStackTrace();
			log.error("Creating the channel to the Broker failed");
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
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			log.fatal("createSSLContext: Initializing the keyManagerFactory failed");
			throw new ServiceConfigurationError("Initializing the keyManagerFactory failed for the SSLContext failed",
					e);
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
	public static ConcurrentHashMap<Integer, Boolean> initPortAllocationMap(ConcurrentHashMap<Integer, Boolean> map,
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
			GatewayMain.portAllocationMap.put(serverSocketPort, false);
		}
		return serverSocketPort;
	}

	public static void makeServerSocketFree(Integer serverSocketPort) {
		GatewayMain.portAllocationMap.put(serverSocketPort, true);
	}

}
