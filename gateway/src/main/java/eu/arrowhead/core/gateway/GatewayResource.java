package eu.arrowhead.core.gateway;

import eu.arrowhead.common.messages.ConnectToConsumerRequest;
import eu.arrowhead.common.messages.ConnectToConsumerResponse;
import eu.arrowhead.common.messages.ConnectToProviderRequest;
import eu.arrowhead.common.messages.ConnectToProviderResponse;
import eu.arrowhead.core.gateway.model.GatewaySession;
import java.io.IOException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;

/**
 * This is the REST resource for the Gateway Core System.
 */
@Path("gateway")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GatewayResource {

  private static final Logger log = Logger.getLogger(GatewayResource.class.getName());

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String getIt() {
    return "This is the Gateway Resource. REST methods: connectToProvider, connectToConsumer.";
  }

  @PUT
  @Path("connectToProvider")
  public Response connectToProvider(ConnectToProviderRequest connectionRequest) {
    String queueName = String.valueOf(System.currentTimeMillis()).concat(String.valueOf(Math.random())).replace(".", "");
    String controlQueueName = queueName.concat("_control");

    if (!connectionRequest.getIsSecure()) {
      // TODO sanity check on the success of the channel create, handle the error
      GatewaySession gatewaySession = GatewayService
          .createInsecureChannel(connectionRequest.getBrokerHost(), connectionRequest.getBrokerPort(), queueName, controlQueueName);
      try {
        GatewayService.communicateWithProviderInsecure(gatewaySession, queueName, controlQueueName, connectionRequest);
      } catch (IOException e) {
        log.error("ConnectToProvider(insecure): I/O exception occured");
        e.printStackTrace();
        // NOTE return error? this should probably trip something up
      }
    } else {
      // TODO sanity check on the success of the channel create, handle the error
      GatewaySession gatewaySession = GatewayService
          .createSecureChannel(connectionRequest.getBrokerHost(), connectionRequest.getBrokerPort(), queueName, controlQueueName);
      try {
        GatewayService.communicateWithProviderSecure(gatewaySession, queueName, controlQueueName, connectionRequest);
      } catch (IOException e) {
        log.error("ConnectToProvider(secure): I/O exception occured");
        e.printStackTrace();
        // NOTE return error? this should probably trip something up
      }

    }

    // TODO: PayloadEncryption instead of null
    ConnectToProviderResponse response = new ConnectToProviderResponse(queueName, controlQueueName, null);
    return Response.status(200).entity(response).build();
  }

  @PUT
  @Path("connectToConsumer")
  public Response connectToConsumer(ConnectToConsumerRequest connectionRequest) {
    Integer serverSocketPort = GatewayService.getAvailablePort();

    if (connectionRequest.getIsSecure()) {
      SecureServerSocketThread secureThread = new SecureServerSocketThread(serverSocketPort, connectionRequest);
      secureThread.start();
    } else {
      InsecureServerSocketThread insecureThread = new InsecureServerSocketThread(serverSocketPort, connectionRequest);
      insecureThread.start();
    }

    ConnectToConsumerResponse response = new ConnectToConsumerResponse(serverSocketPort);
    log.info("Returning the ConnectToConsumerResponse to the Gatekeeper");
    return Response.status(200).entity(response).build();
  }

}