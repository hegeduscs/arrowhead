package eu.arrowhead.core.gatekeeper;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.Utility;
import eu.arrowhead.common.database.ArrowheadCloud;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.Broker;
import eu.arrowhead.common.database.CoreSystem;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.messages.ConnectToConsumerRequest;
import eu.arrowhead.common.messages.ConnectToConsumerResponse;
import eu.arrowhead.common.messages.GSDAnswer;
import eu.arrowhead.common.messages.GSDPoll;
import eu.arrowhead.common.messages.GSDRequestForm;
import eu.arrowhead.common.messages.GSDResult;
import eu.arrowhead.common.messages.GatewayConnectionInfo;
import eu.arrowhead.common.messages.ICNEnd;
import eu.arrowhead.common.messages.ICNProposal;
import eu.arrowhead.common.messages.ICNRequestForm;
import eu.arrowhead.common.messages.ICNResult;
import eu.arrowhead.common.messages.OrchestrationForm;
import eu.arrowhead.common.messages.OrchestrationResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import org.apache.log4j.Logger;

/**
 * This is the REST resource for the Gatekeeper Core System.
 */
@Path("gatekeeper")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GatekeeperOutboundResource {

  private static final Logger log = Logger.getLogger(GatekeeperOutboundResource.class.getName());
  private static final DatabaseManager dm = DatabaseManager.getInstance();


  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String getIt() {
    return "This is the outbound Gatekeeper Resource. Offering resources at: init_gsd, init_icn.";
  }

  /**
   * This function represents the consumer-side of GlobalServiceDiscovery, where the GateKeeper of the consumer System tries to find a provider Cloud
   * for the requested Service.
   *
   * @return GSDResult
   */
  @PUT
  @Path("init_gsd")
  public Response GSDRequest(GSDRequestForm requestForm, @Context ContainerRequestContext requestContext) {
    if (!requestForm.isValid()) {
      log.error("GSDRequest BadPayloadException");
      throw new BadPayloadException("Bad payload: requestedService is missing or it is not valid.", Status.BAD_REQUEST.getStatusCode(),
                                    BadPayloadException.class.getName(), requestContext.getUriInfo().getAbsolutePath().toString());
    }

    ArrowheadCloud ownCloud = Utility.getOwnCloud();
    GSDPoll gsdPoll = new GSDPoll(requestForm.getRequestedService(), ownCloud);

    // If no preferred Clouds were given, send GSD poll requests to the neighbor Clouds
    List<String> cloudURIs = new ArrayList<>();
    if (requestForm.getSearchPerimeter().isEmpty()) {
      cloudURIs = Utility.getNeighborCloudURIs();
    }
    // If there are preferred Clouds given, send GSD poll requests there
    else {
      String uri;
      for (ArrowheadCloud cloud : requestForm.getSearchPerimeter()) {
        try {
          uri = Utility.getUri(cloud.getAddress(), cloud.getPort(), cloud.getGatekeeperServiceURI(), false);
        }
        // We skip the clouds with missing information
        catch (NullPointerException ex) {
          continue;
        }
        cloudURIs.add(uri);
      }
    }
    log.info("Sending GSD poll request to " + cloudURIs.size() + " clouds.");

    // Finalizing the URIs, process the responses
    List<GSDAnswer> gsdAnswerList = new ArrayList<>();
    Response response;
    for (String uri : cloudURIs) {
      uri = UriBuilder.fromPath(uri).path("gsd_poll").toString();
      try {
        response = Utility.sendRequest(uri, "PUT", gsdPoll, GatekeeperMain.outboundClientContext);
      }
      // We skip those that did not respond positively, add the rest to the result list
      catch (RuntimeException ex) {
        continue;
      }
      gsdAnswerList.add(response.readEntity(GSDAnswer.class));
    }

    // Sending back the results. The orchestrator will validate the results (result list might be empty) and decide how to proceed.
    GSDResult gsdResult = new GSDResult(gsdAnswerList);
    log.info("GSDRequest: Sending " + gsdAnswerList.size() + " GSDPoll results to Orchestrator.");
    return Response.status(Status.OK).entity(gsdResult).build();
  }


  /**
   * This function represents the consumer-side of InterCloudNegotiations, where the Gatekeeper sends information about the requester System. (SSL
   * secured)
   *
   * @return ICNResult
   */
  @PUT
  @Path("init_icn")
  public Response ICNRequest(ICNRequestForm requestForm, @Context ContainerRequestContext requestContext) {
    if (!requestForm.isValid()) {
      log.error("ICNRequest BadPayloadException");
      throw new BadPayloadException("Bad payload: missing/incomplete ICNRequestForm.", Status.BAD_REQUEST.getStatusCode(),
                                    BadPayloadException.class.getName(), requestContext.getUriInfo().getAbsolutePath().toString());
    }

    // Getting the list of preferred brokers from database + read in the use_gateway property
    List<Broker> preferredBrokers = dm.getAll(Broker.class, null);
    boolean useGateway = Boolean.valueOf(GatekeeperMain.getProp().getProperty("use_gateway", "false"));
    requestForm.getNegotiationFlags().put("useGateway", useGateway);

    // Compiling the payload and then getting the request URI
    ICNProposal icnProposal = new ICNProposal(requestForm.getRequestedService(), Utility.getOwnCloud(), requestForm.getRequesterSystem(),
                                              requestForm.getPreferredSystems(), requestForm.getNegotiationFlags(),
                                              requestForm.getAuthenticationInfo(), preferredBrokers, GatekeeperMain.timeout,
                                              Utility.getCoreSystem("gateway").getAuthenticationInfo());

    String icnUri = Utility.getUri(requestForm.getTargetCloud().getAddress(), requestForm.getTargetCloud().getPort(),
                                   requestForm.getTargetCloud().getGatekeeperServiceURI(), false);
    icnUri = UriBuilder.fromPath(icnUri).path("icn_proposal").toString();
    // Sending the request, the response payload is use_gateway flag dependent
    Response response = Utility.sendRequest(icnUri, "PUT", icnProposal, GatekeeperMain.outboundClientContext);

    // If the gateway services are not requested, then just send back the ICN results to the Orchestrator right away
    if (!useGateway) {
      ICNResult icnResult = response.readEntity(ICNResult.class);
      log.info("ICNRequest: returning ICNResult to Orchestrator.");
      return Response.status(response.getStatus()).entity(icnResult).build();
    }
    // The partner Gatekeeper will return an ICNEnd if use_gateway = true
    ICNEnd icnEnd = response.readEntity(ICNEnd.class);

    // Compiling the gateway request payload
    String gatewayURI = Utility.getGatewayUri();
    gatewayURI = UriBuilder.fromPath(gatewayURI).path("connectToConsumer").toString();

    Map<String, String> metadata = requestForm.getRequestedService().getServiceMetadata();
    boolean isSecure = metadata.containsKey("security") && !metadata.get("security").equals("none");
    GatewayConnectionInfo gwConnInfo = icnEnd.getGatewayConnInfo();
    ConnectToConsumerRequest connectionRequest = new ConnectToConsumerRequest(gwConnInfo.getBrokerName(), gwConnInfo.getBrokerPort(),
                                                                              gwConnInfo.getQueueName(), gwConnInfo.getControlQueueName(),
                                                                              requestForm.getRequesterSystem(), isSecure, GatekeeperMain.timeout,
                                                                              gwConnInfo.getGatewayPublicKey());
    //Sending the gateway request and parsing the response
    Response gatewayResponse = Utility.sendRequest(gatewayURI, "PUT", connectionRequest, GatekeeperMain.outboundServerContext);
    ConnectToConsumerResponse connectToConsumerResponse = gatewayResponse.readEntity(ConnectToConsumerResponse.class);

    CoreSystem gateway = Utility.getCoreSystem("gateway");
    ArrowheadSystem gatewaySystem = new ArrowheadSystem();
    gatewaySystem.setSystemGroup("coresystems");
    gatewaySystem.setSystemName(gateway.getSystemName());
    gatewaySystem.setAddress(gateway.getAddress());
    gatewaySystem.setPort(connectToConsumerResponse.getServerSocketPort());
    gatewaySystem.setAuthenticationInfo(gateway.getAuthenticationInfo());
    icnEnd.getOrchestrationForm().setProvider(gatewaySystem);
    List<OrchestrationForm> orchResponse = new ArrayList<>();
    orchResponse.add(icnEnd.getOrchestrationForm());
    ICNResult icnResult = new ICNResult(new OrchestrationResponse(orchResponse));

    log.info("ICNRequest: returning ICNResult to Orchestrator.");
    return Response.status(response.getStatus()).entity(icnResult).build();
  }

}
