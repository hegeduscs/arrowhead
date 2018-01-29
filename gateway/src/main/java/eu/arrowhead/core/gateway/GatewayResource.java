package eu.arrowhead.core.gateway;

import eu.arrowhead.common.Utility;
import eu.arrowhead.common.messages.ConnectToConsumerRequest;
import eu.arrowhead.common.messages.ConnectToConsumerResponse;
import eu.arrowhead.common.messages.ConnectToProviderRequest;
import eu.arrowhead.common.messages.ConnectToProviderResponse;
import eu.arrowhead.core.gateway.model.ActiveSession;
import eu.arrowhead.core.gateway.model.GatewaySession;
import eu.arrowhead.core.gateway.thread.InsecureServerSocketThread;
import eu.arrowhead.core.gateway.thread.InsecureSocketThread;
import eu.arrowhead.core.gateway.thread.SecureServerSocketThread;
import eu.arrowhead.core.gateway.thread.SecureSocketThread;
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

  @GET
  @Path("management")
  @Produces(MediaType.TEXT_PLAIN)
  public String sessionManagement() {
    if (GatewayService.activeSessions.isEmpty()) {
      return "There are no active sessions.";
    } else {
      return Utility.toPrettyJson(null, GatewayService.activeSessions);
    }
  }

  @PUT
  @Path("connectToProvider")
  public Response connectToProvider(ConnectToProviderRequest connectionRequest) {
    String queueName = String.valueOf(System.currentTimeMillis()).concat(String.valueOf(Math.random())).replace(".",
        "");
    String controlQueueName = queueName.concat("_control");

    // TODO sanity check on the success of the channel create, handle the error
    GatewaySession gatewaySession = GatewayService.createChannel(connectionRequest.getBrokerHost(),
        connectionRequest.getBrokerPort(), queueName, controlQueueName, connectionRequest.getIsSecure());

    if (connectionRequest.getIsSecure()) {
      SecureSocketThread secureThread = new SecureSocketThread(gatewaySession, queueName, controlQueueName,
          connectionRequest);
      secureThread.start();
    } else {
      InsecureSocketThread insecureThread = new InsecureSocketThread(gatewaySession, queueName, controlQueueName,
          connectionRequest);
      insecureThread.start();
    }

    ConnectToProviderResponse response = new ConnectToProviderResponse(queueName, controlQueueName, null);
    return Response.status(200).entity(response).build();
  }

  @PUT
  @Path("connectToConsumer")
  public Response connectToConsumer(ConnectToConsumerRequest connectionRequest) {
    Integer serverSocketPort = GatewayService.getAvailablePort();

    // Add the session to the management queue
    ActiveSession activeSession = new ActiveSession(connectionRequest.getConsumer(), connectionRequest.getBrokerName(),
        connectionRequest.getBrokerPort(), serverSocketPort, connectionRequest.getIsSecure());
    GatewayService.activeSessions.put(connectionRequest.getQueueName(), activeSession);

    GatewaySession gatewaySession = GatewayService.createChannel(connectionRequest.getBrokerName(),
        connectionRequest.getBrokerPort(), connectionRequest.getQueueName(), connectionRequest.getControlQueueName(),
        connectionRequest.getIsSecure());

    if (connectionRequest.getIsSecure()) {
      SecureServerSocketThread secureThread = new SecureServerSocketThread(gatewaySession, serverSocketPort,
          connectionRequest);
      secureThread.start();
    } else {
      InsecureServerSocketThread insecureThread = new InsecureServerSocketThread(gatewaySession, serverSocketPort,
          connectionRequest);
      insecureThread.start();
    }

    ConnectToConsumerResponse response = new ConnectToConsumerResponse(serverSocketPort);
    log.info("Returning the ConnectToConsumerResponse to the Gatekeeper");
    return Response.status(200).entity(response).build();
  }

}
