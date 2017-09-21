package eu.arrowhead.core.gateway;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import eu.arrowhead.common.messages.GatewayAtConsumerRequest;

public class InsecureServerSocketThread extends Thread {

	private int port;
	private ServerSocket serverSocket = null;
	private GatewayAtConsumerRequest connectionRequest;

	public InsecureServerSocketThread(int port, GatewayAtConsumerRequest connectionRequest) {
		this.port = port;
		this.connectionRequest = connectionRequest;
	}

	public void run() {

		try {
			serverSocket = new ServerSocket(port);
			System.out.println("Insecure serverSocket is now running at port: " + port);
		} catch (IOException e) {
			e.printStackTrace();
			GatewayResource.log.fatal("Creating insecure ServerSocket failed");
		}

		try {
			Socket consumerSocket = serverSocket.accept();
			//String consumerCN = consumerSocket.get
			InputStream inConsumer = consumerSocket.getInputStream();
			OutputStream outConsumer = consumerSocket.getOutputStream();
			
			//Get the request from the Consumer
			byte[] inputFromConsumer = new byte[1024];
			byte[] inputFromConsumerFinal = new byte[inConsumer.read(inputFromConsumer)];
			for (int i = 0; i < inputFromConsumerFinal.length; i++) {
				inputFromConsumerFinal[i] = inputFromConsumer[i];
				
			Channel channel = GatewayService.createChannel(connectionRequest.getBrokerName(),
						connectionRequest.getBrokerPort(), connectionRequest.getQueueName());
			channel.basicPublish("", connectionRequest.getQueueName(), null, inputFromConsumerFinal);
			
			Consumer consumer = new DefaultConsumer(channel) {
			      @Override
					public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
			          throws IOException {
			        outConsumer.write(body);			        
			      }
			    };
			    channel.basicConsume(connectionRequest.getQueueName(), true, consumer);
			    channel.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
			GatewayResource.log.fatal("Creating insecure clientSocket failed");
		}

		
		
	}

}
