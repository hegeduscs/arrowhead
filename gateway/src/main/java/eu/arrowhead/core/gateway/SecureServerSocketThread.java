package eu.arrowhead.core.gateway;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import eu.arrowhead.common.messages.GatewayAtConsumerRequest;
import eu.arrowhead.common.security.SecurityUtils;

public class SecureServerSocketThread extends Thread {

	private String consumerCN;
	private String consumerIP;
	private int port;
	private int timeout;
	private SSLContext sslContext;
	private GatewayAtConsumerRequest connectionRequest;

	private SSLServerSocket sslServerSocket = null;
	private SSLSocket sslConsumerSocket = null;

	public SecureServerSocketThread(int port, GatewayAtConsumerRequest connectionRequest) {
		this.port = port;
		this.connectionRequest = connectionRequest;
	}

	public void run() {
		timeout = Integer.parseInt(GatewayMain.getProp().getProperty("timeout"));
		consumerCN = connectionRequest.getConsumer().getSystemName();
		consumerIP = connectionRequest.getConsumer().getAddress();

		sslContext = GatewayService.createSSLContext();
		// Socket for server to listen at.
		SSLServerSocketFactory serverFactory = sslContext.getServerSocketFactory();
		try {
			sslServerSocket = (SSLServerSocket) serverFactory.createServerSocket(port);
			sslServerSocket.setNeedClientAuth(true);
			System.out.println("Secure serverSocket is now running at port: " + port + "\n");
		} catch (IOException e) {
			e.printStackTrace();
			GatewayResource.log.fatal("Creating secure serverSocket failed.");
		}

		try {
			// Accept a client connection once Server receives one.
			sslConsumerSocket = (SSLSocket) sslServerSocket.accept();
			SSLSession consumerSession = sslConsumerSocket.getSession();
			
			String consumerIPFromCert = consumerSession.getPeerHost();
			long sessionCreationTime = consumerSession.getCreationTime();
			Certificate[] servercerts = consumerSession.getPeerCertificates();
			X509Certificate cert = (X509Certificate) servercerts[0];
			String subjectname = cert.getSubjectDN().getName();
			String consumerCNFromCert = SecurityUtils.getCertCNFromSubject(subjectname);

			if (!consumerCN.equals(consumerCNFromCert) | !consumerIP.equals(consumerIPFromCert)) {
				GatewayMain.getPortAllocationMap().replace(port, false, true);
				GatewayResource.log.fatal("SecureServerThread: Consumer CNs or IPs not equal");
				throw new RuntimeException("SecureServerThread: Consumer CNs or IPs not equal");
			}
			InputStream inConsumer = sslConsumerSocket.getInputStream();
			OutputStream outConsumer = sslConsumerSocket.getOutputStream();

			// Get the request from the Consumer
			byte[] inputFromConsumer = new byte[1024];
			byte[] inputFromConsumerFinal = new byte[inConsumer.read(inputFromConsumer)];
			for (int i = 0; i < inputFromConsumerFinal.length; i++) {
				inputFromConsumerFinal[i] = inputFromConsumer[i];
			}

			Channel channel = GatewayService.createSecureChannel(connectionRequest.getBrokerName(),
					connectionRequest.getBrokerPort(), connectionRequest.getQueueName());

			channel.basicPublish("", connectionRequest.getQueueName(), null, inputFromConsumerFinal);

			Consumer consumer = new DefaultConsumer(channel) {
				@Override
				public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
						byte[] body) throws IOException {
					outConsumer.write(body);
				}
			};
			channel.basicConsume(connectionRequest.getQueueName(), true, consumer);
			channel.close();

		} catch (IOException e) {
			e.printStackTrace();
			GatewayResource.log.fatal("SecureServerThread: Creating secure clientSocket failed.");
		}

	}

}
