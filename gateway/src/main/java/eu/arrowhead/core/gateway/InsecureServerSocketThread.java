package eu.arrowhead.core.gateway;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;

import eu.arrowhead.common.messages.ConnectToConsumerRequest;

public class InsecureServerSocketThread extends Thread {

	private int port;
	private ServerSocket serverSocket;
	private ConnectToConsumerRequest connectionRequest;

	public InsecureServerSocketThread(int port, ConnectToConsumerRequest connectionRequest) {
		this.port = port;
		this.connectionRequest = connectionRequest;
	}

	public void run() {

		try {
			serverSocket = new ServerSocket(port);
			System.out.println("Insecure serverSocket is now running at port: " + port);
		} catch (IOException e) {
			e.printStackTrace();
			GatewayResource.log.error("Creating insecure ServerSocket failed");
		}

		try {
			// Create socket for Consumer
			Socket consumerSocket = serverSocket.accept();
			InputStream inConsumer = consumerSocket.getInputStream();
			OutputStream outConsumer = consumerSocket.getOutputStream();

			// Get the request from the Consumer
			byte[] inputFromConsumer = new byte[1024];
			byte[] inputFromConsumerFinal = new byte[inConsumer.read(inputFromConsumer)];
			for (int i = 0; i < inputFromConsumerFinal.length; i++) {
				inputFromConsumerFinal[i] = inputFromConsumer[i];
			}

			// Create a channel
			GatewaySession gatewaySession = GatewayService.createChannel(connectionRequest.getBrokerName(),
					connectionRequest.getBrokerPort(), connectionRequest.getQueueName(),
					connectionRequest.getControlQueueName());
			Channel channel = gatewaySession.getChannel();

			channel.basicPublish("", connectionRequest.getQueueName(), null, inputFromConsumerFinal);

			// Get the response and the control messages
			GetResponse controlMessage = channel.basicGet(connectionRequest.getControlQueueName(), false);
			while (controlMessage == null || !(new String(controlMessage.getBody()).equals("close"))) {
				GetResponse message = channel.basicGet(connectionRequest.getQueueName(), false);
				if (message == null) {
					System.out.println("No message retrieved");
				} else {
					outConsumer.write(message.getBody());
				}
				controlMessage = channel.basicGet(connectionRequest.getControlQueueName(), false);
			}

			// Close sockets and the connection
			channel.close();
			gatewaySession.getConnection().close();
			consumerSocket.close();
			serverSocket.close();

		} catch (IOException e) {
			e.printStackTrace();
			GatewayResource.log.error("Creating insecure clientSocket failed");
		}

	}

}
