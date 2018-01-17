package eu.arrowhead.core.gateway.thread;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import eu.arrowhead.common.messages.ConnectToConsumerRequest;
import eu.arrowhead.core.gateway.GatewayService;
import eu.arrowhead.core.gateway.model.GatewaySession;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import org.apache.log4j.Logger;

public class InsecureServerSocketThread extends Thread {

	private int port;
	private ServerSocket serverSocket;
	private ConnectToConsumerRequest connectionRequest;
	private static final Logger log = Logger.getLogger(InsecureServerSocketThread.class.getName());
	private GatewaySession gatewaySession;

	public InsecureServerSocketThread(GatewaySession gatewaySession, int port,
			ConnectToConsumerRequest connectionRequest) {
		this.port = port;
		this.connectionRequest = connectionRequest;
		this.gatewaySession = gatewaySession;
	}

	// TODO narrower try-catches + maybe create 1 (or 2 with secure version) method
	// for the while loop
	// part which has 4 different copies in 4 methods
	public void run() {

		try {
			serverSocket = new ServerSocket(port);
			System.out.println("Insecure serverSocket is now running at port: " + port);
			log.info("Insecure serverSocket is now running at port: " + port);
		} catch (IOException e) {
			log.error("Creating insecure ServerSocket failed");
			throw new RuntimeException(e.getMessage(), e);
		}

		try {
			// Create socket for Consumer
			serverSocket.setSoTimeout(connectionRequest.getTimeout());
			Socket consumerSocket = serverSocket.accept();
			consumerSocket.setSoTimeout(connectionRequest.getTimeout());

			InputStream inConsumer = consumerSocket.getInputStream();
			OutputStream outConsumer = consumerSocket.getOutputStream();
			log.info("Create socket for Consumer");
			Channel channel = gatewaySession.getChannel();

			try {

				Consumer consumer = new DefaultConsumer(channel) {
					@Override
					public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
							byte[] body) throws IOException {
						outConsumer.write(body);
						System.out.println("Broker response: ");
						System.out.println(new String(body));
					}

				};

				Consumer controlConsumer = new DefaultConsumer(channel) {
					@Override
					public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
							byte[] body) throws IOException {
						if (new String(body).equals("close")) {
							GatewayService.makeServerSocketFree(port);
							try {
								channel.close();
								gatewaySession.getConnection().close();
							} catch (AlreadyClosedException e) {
								log.info("Channel already closed by Broker");
								throw new RuntimeException(e.getMessage(), e);
							}
							consumerSocket.close();
							serverSocket.close();
							log.info("ConsumerSocket closed");
						}
					}
				};

				while (true) {
					try {
						// Get the request from the Consumer
						byte[] inputFromConsumer = new byte[1024];
						byte[] inputFromConsumerFinal = null;

						inputFromConsumerFinal = new byte[inConsumer.read(inputFromConsumer)];

						System.arraycopy(inputFromConsumer, 0, inputFromConsumerFinal, 0,
								inputFromConsumerFinal.length);

						System.out.println("Consumer's final request:");
						System.out.println(new String(inputFromConsumerFinal));

						channel.basicPublish("", connectionRequest.getQueueName(), null, inputFromConsumerFinal);
						log.info("Publishing the request to the queue");

						channel.basicConsume(connectionRequest.getQueueName().concat("_resp"), true, consumer);
						channel.basicConsume(connectionRequest.getControlQueueName().concat("_resp"), true,
								controlConsumer);
					} catch (NegativeArraySizeException e) {						
						log.error("Socket closed by remote partner");
						GatewayService.makeServerSocketFree(port);
						try {
							channel.close();
							gatewaySession.getConnection().close();
						} catch (AlreadyClosedException error) {
							log.info("Channel already closed by Broker");
						}
						consumerSocket.close();
						serverSocket.close();
						log.info("ConsumerSocket closed");
						throw new RuntimeException(e.getMessage(), e);
					}
				}
			} catch (SocketException e) {
				log.error("Socket closed by remote partner");
				GatewayService.makeServerSocketFree(port);
				try {
					channel.close();
					gatewaySession.getConnection().close();
				} catch (AlreadyClosedException error) {
					log.info("Channel already closed by Broker");
				}
				consumerSocket.close();
				serverSocket.close();
				log.info("ConsumerSocket closed");
			}

		} catch (IOException e) {
			e.printStackTrace();
			log.error("Creating insecure clientSocket failed");
		}
	}

}
