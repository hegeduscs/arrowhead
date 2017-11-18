package eu.arrowhead.core.gateway.thread;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import eu.arrowhead.common.messages.ConnectToProviderRequest;
import eu.arrowhead.core.gateway.model.GatewaySession;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import org.apache.log4j.Logger;

public class InsecureSocketThread extends Thread {

  private GatewaySession gatewaySession;
	private String queueName;
	private String controlQueueName;
	private ConnectToProviderRequest connectionRequest;
	private static final Logger log = Logger.getLogger(InsecureSocketThread.class.getName());

	public InsecureSocketThread(GatewaySession gatewaySession, String queueName, String controlQueueName,
			ConnectToProviderRequest connectionRequest) {
		this.gatewaySession = gatewaySession;
		this.queueName = queueName;
		this.controlQueueName = controlQueueName;
		this.connectionRequest = connectionRequest;
	}

	public void run() {
		try {
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
					System.arraycopy(inputFromProvider, 0, inputFromProviderFinal, 0, inputFromProviderFinal.length);
					channel.basicPublish("", queueName, null, inputFromProviderFinal);
				}
				controlMessage = channel.basicGet(controlQueueName, false);
			}
			// Close sockets and the connection
			channel.close();
			gatewaySession.getConnection().close();
			if (providerSocket != null) {
				providerSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
			log.error("ConnectToProvider(insecure): I/O exception occured");
		}
	}

}
