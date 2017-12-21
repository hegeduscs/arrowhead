package eu.arrowhead.core.gateway.thread;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;
import eu.arrowhead.common.messages.ConnectToProviderRequest;
import eu.arrowhead.core.gateway.GatewayService;
import eu.arrowhead.core.gateway.model.GatewaySession;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

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
			SSLSocket sslProviderSocket = (SSLSocket) clientFactory.createSocket(
					connectionRequest.getProvider().getAddress(), connectionRequest.getProvider().getPort());
			sslProviderSocket.setSoTimeout(connectionRequest.getTimeout());
			InputStream inProvider = sslProviderSocket.getInputStream();
			OutputStream outProvider = sslProviderSocket.getOutputStream();
			log.info("Create SSLsocket for Provider");

			// Receiving messages through AMQP Broker
			try {
				Consumer consumer = new DefaultConsumer(channel) {
					@Override
					public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
							byte[] body) throws IOException {
						outProvider.write(body);
						log.info("Sending the request to Provider");
						// get the answer from Provider
						byte[] inputFromProvider = new byte[1024];
						byte[] inputFromProviderFinal = new byte[inProvider.read(inputFromProvider)];
						System.arraycopy(inputFromProvider, 0, inputFromProviderFinal, 0,
								inputFromProviderFinal.length);
						log.info("Sending the response to Consumer");
						channel.basicPublish("", queueName.concat("_resp"), null, inputFromProviderFinal);
						channel.basicPublish("", controlQueueName.concat("_resp"), null, "close".getBytes());
					}
				};
				channel.basicConsume(queueName, true, consumer);

				Consumer controlConsumer = new DefaultConsumer(channel) {
					@Override
					public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
							byte[] body) throws IOException {
						if (new String(body).equals("close")) {
							sslProviderSocket.close();
							channel.close();
							gatewaySession.getConnection().close();
							log.info("SSLProviderSocket closed");
						}
					}
				};
				channel.basicConsume(controlQueueName, true, controlConsumer);

			} catch (SocketException e) {
				log.error("Socket closed by remote partner");
				sslProviderSocket.close();
				channel.close();
				gatewaySession.getConnection().close();
			}
		} catch (IOException e) {
			e.printStackTrace();
			log.error("ConnectToProvider(secure): I/O exception occured");
		}
	}

}
