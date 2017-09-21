package eu.arrowhead.core.gateway;

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

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;

import eu.arrowhead.common.messages.GatewayAtConsumerRequest;
import eu.arrowhead.common.messages.GatewayAtConsumerResponse;
import eu.arrowhead.common.messages.GatewayAtProviderRequest;
import eu.arrowhead.common.security.SecurityUtils;

/**
 * This is the REST resource for the Gateway Core System.
 */
@Path("gateway")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GatewayResource {

	public static Logger log = Logger.getLogger(GatewayResource.class.getName());

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getIt() {
		return "This is the Gateway Resource. " + "REST methods: connect.";
	}

	@PUT
	@Path("connectAtProvider")
	public Response connectionResource(GatewayAtProviderRequest connectionRequest) {
		// TODO: mi legyen a queueName
		String queueName = "";
		String providerAddress = connectionRequest.getProvider().getAddress();
		int providerPort = connectionRequest.getProvider().getPort();
		if (connectionRequest.getIsSecure() == false) {
			Channel channel = GatewayService.createChannel(connectionRequest.getBrokerName(),
					connectionRequest.getBrokerPort(), queueName);
			try {
				Consumer consumer = new DefaultConsumer(channel) {
					@Override
					public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
							byte[] body) throws IOException {
						// send the request to Provider
						Socket providerSocket = new Socket(providerAddress, providerPort);
						InputStream inProvider = providerSocket.getInputStream();
						OutputStream outProvider = providerSocket.getOutputStream();
						outProvider.write(body);

						// get the answer from Provider
						byte[] inputFromProvider = new byte[1024];
						byte[] inputFromProviderFinal = new byte[inProvider.read(inputFromProvider)];
						for (int i = 0; i < inputFromProviderFinal.length; i++) {
							inputFromProviderFinal[i] = inputFromProvider[i];
						}

						channel.basicPublish("", queueName, null, inputFromProviderFinal);
						inProvider.close();
						outProvider.close();
						providerSocket.close();
					}
				};
				channel.basicConsume(queueName, true, consumer);

			} catch (IOException e) {

			}

		} else {
			Channel channel = GatewayService.createSecureChannel(connectionRequest.getBrokerName(),
					connectionRequest.getBrokerPort(), queueName);

		}

		return Response.status(200).build();
	}

	@PUT
	@Path("connectAtConsumer")
	public Response connectionResource(GatewayAtConsumerRequest connectionRequest) {
		Integer serverSocketPort = GatewayService.getAvailablePort();

		if (connectionRequest.getIsSecure() == true) {
			// TODO: test data; insert the real consumerCN and consumerIP to constr.
			SecureServerSocketThread secureThread = new SecureServerSocketThread(serverSocketPort, connectionRequest);
			secureThread.start();

		} else {
			InsecureServerSocketThread insecureThread = new InsecureServerSocketThread(serverSocketPort,
					connectionRequest);
			insecureThread.start();
		}

		log.info("GatewayResource: returning the GatewayAtConsumerResponse to the Gatekeeper");
		GatewayAtConsumerResponse response = new GatewayAtConsumerResponse(serverSocketPort);
		return Response.status(200).entity(response).build();
	}

}