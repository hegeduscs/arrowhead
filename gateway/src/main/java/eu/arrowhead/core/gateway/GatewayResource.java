package eu.arrowhead.core.gateway;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

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

import eu.arrowhead.common.messages.ConnectToConsumerRequest;
import eu.arrowhead.common.messages.ConnectToConsumerResponse;
import eu.arrowhead.common.messages.ConnectToProviderRequest;
import eu.arrowhead.common.messages.ConnectToProviderResponse;

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
		return "This is the Gateway Resource. " + "REST methods: connectToProvider, connectToConsumer.";
	}

	@PUT
	@Path("connectToProvider")
	public Response providerConnecionResource(ConnectToProviderRequest connectionRequest) {
		String queueName = String.valueOf(System.currentTimeMillis()).concat(String.valueOf(Math.random())).replace(".",
				"");

		String controlQueueName = queueName.concat("_control");

		if (connectionRequest.getIsSecure() == false) {
			GatewaySession gatewaySession = GatewayService.createChannel(connectionRequest.getBrokerName(),
					connectionRequest.getBrokerPort(), queueName, controlQueueName);
			try {
				GatewayService.communicateWithProviderInsecure(gatewaySession, queueName, controlQueueName,
						connectionRequest);
			} catch (IOException e) {
				log.error("ConnectToProvider(insecure): I/O exception occured");
			}
		} else {
			GatewaySession gatewaySession = GatewayService.createSecureChannel(connectionRequest.getBrokerName(),
					connectionRequest.getBrokerPort(), queueName, controlQueueName);
			try {
				GatewayService.communicateWithProviderSecure(gatewaySession, queueName, controlQueueName,
						connectionRequest);
			} catch (IOException e) {
				log.error("ConnectToProvider(secure): I/O exception occured");
			}

		}

		// TODO: PayloadEncryption instead of null
		ConnectToProviderResponse response = new ConnectToProviderResponse(queueName, controlQueueName, null);
		return Response.status(200).entity(response).build();
	}

	@PUT
	@Path("connectToConsumer")
	public Response consumerConnectionResource(ConnectToConsumerRequest connectionRequest) {
		Integer serverSocketPort = GatewayService.getAvailablePort();

		if (connectionRequest.getIsSecure() == true) {

			SecureServerSocketThread secureThread = new SecureServerSocketThread(serverSocketPort, connectionRequest);
			secureThread.start();

		} else {
			InsecureServerSocketThread insecureThread = new InsecureServerSocketThread(serverSocketPort,
					connectionRequest);
			insecureThread.start();
		}

		log.info("GatewayResource: returning the ConnectToConsumerResponse to the Gatekeeper");
		ConnectToConsumerResponse response = new ConnectToConsumerResponse(serverSocketPort);
		return Response.status(200).entity(response).build();
	}

}