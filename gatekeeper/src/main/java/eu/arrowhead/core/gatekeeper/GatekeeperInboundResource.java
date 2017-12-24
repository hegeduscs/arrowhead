package eu.arrowhead.core.gatekeeper;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.Utility;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.Broker;
import eu.arrowhead.common.exception.AuthenticationException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.exception.ErrorMessage;
import eu.arrowhead.common.messages.ConnectToProviderRequest;
import eu.arrowhead.common.messages.ConnectToProviderResponse;
import eu.arrowhead.common.messages.GSDAnswer;
import eu.arrowhead.common.messages.GSDPoll;
import eu.arrowhead.common.messages.GatewayConnectionInfo;
import eu.arrowhead.common.messages.ICNEnd;
import eu.arrowhead.common.messages.ICNProposal;
import eu.arrowhead.common.messages.ICNResult;
import eu.arrowhead.common.messages.InterCloudAuthRequest;
import eu.arrowhead.common.messages.InterCloudAuthResponse;
import eu.arrowhead.common.messages.OrchestrationResponse;
import eu.arrowhead.common.messages.PreferredProvider;
import eu.arrowhead.common.messages.ServiceQueryForm;
import eu.arrowhead.common.messages.ServiceQueryResult;
import eu.arrowhead.common.messages.ServiceRequestForm;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import org.apache.log4j.Logger;

@Path("gatekeeper")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GatekeeperInboundResource {

  private static final Logger log = Logger.getLogger(GatekeeperInboundResource.class.getName());

  private static final DatabaseManager dm = DatabaseManager.getInstance();


  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String getIt() {
    return "This is the inbound Gatekeeper Resource. Offering resources at: gsd_poll, icn_proposal.";
  }

  /**
   * This function represents the provider-side of GlobalServiceDiscovery, where the GateKeeper of the provider Cloud sends back its information if
   * the Authorization and Service Registry polling yields positive results.
   *
   * @return GSDAnswer
   */
  @PUT
  @Path("gsd_poll")
  public Response GSDPoll(GSDPoll gsdPoll) {
    if (!gsdPoll.isValid()) {
      log.error("GSDPoll BadPayloadException");
      throw new BadPayloadException("gsd_poll received bad payload: requestedService/requesterCloud is missing or it is not valid.");
    }

    // Polling the Authorization System about the consumer Cloud
    InterCloudAuthRequest authRequest = new InterCloudAuthRequest(gsdPoll.getRequesterCloud(), gsdPoll.getRequestedService());
    String authUri = Utility.getAuthorizationUri();
    authUri = UriBuilder.fromPath(authUri).path("intercloud").toString();
    Response authResponse = Utility.sendRequest(authUri, "PUT", authRequest);

    // If the consumer Cloud is not authorized an error is returned
    if (!authResponse.readEntity(InterCloudAuthResponse.class).isAuthorized()) {
      log.info("GSD poll: Requester Cloud is UNAUTHORIZED, sending back error");
      ErrorMessage errorMessage = new ErrorMessage("Requester Cloud is UNAUTHORIZED to consume this service, GSD poll failed.", 401,
                                                   AuthenticationException.class.toString());
      return Response.status(Status.UNAUTHORIZED).entity(errorMessage).build();
    }
    // If it is authorized, poll the Service Registry for the requested Service
    else {
      // Compiling the URI and the request payload
      String srUri = Utility.getServiceRegistryUri();
      srUri = UriBuilder.fromPath(srUri).path("query").toString();
      ServiceQueryForm queryForm = new ServiceQueryForm(gsdPoll.getRequestedService(), false, false);

      // Sending back provider Cloud information if the SR poll has results
      Response srResponse = Utility.sendRequest(srUri, "PUT", queryForm);
      ServiceQueryResult result = srResponse.readEntity(ServiceQueryResult.class);
      if (!result.isValid()) {
        log.info("GSD poll: SR query came back empty, sending back error");
        ErrorMessage errorMessage = new ErrorMessage("Service not found in the Service Registry, GSD poll failed.", 404,
                                                     DataNotFoundException.class.toString());
        return Response.status(Status.NOT_FOUND).entity(errorMessage).build();
      }

      log.info("GSDPoll successful, sending back GSDAnswer");
      GSDAnswer answer = new GSDAnswer(gsdPoll.getRequestedService(), Utility.getOwnCloud());
      return Response.status(Status.OK).entity(answer).build();
    }
  }

  /**
   * This function represents the provider-side of InterCloudNegotiations, where the Gatekeeper (after an Orchestration process) sends information
   * about the provider System. (SSL secured)
   *
   * @return ICNEnd
   */
  @PUT
  @Path("icn_proposal")
  public Response ICNProposal(ICNProposal icnProposal) {
    if (!icnProposal.isValid()) {
      log.error("ICNProposal BadPayloadException");
      throw new BadPayloadException("icn_proposal received bad payload: missing/incomplete ICNProposal.");
    }

    // Polling the Authorization System about the consumer Cloud
    InterCloudAuthRequest authRequest = new InterCloudAuthRequest(icnProposal.getRequesterCloud(), icnProposal.getRequestedService());
    String authUri = Utility.getAuthorizationUri();
    authUri = UriBuilder.fromPath(authUri).path("intercloud").toString();
    Response authResponse = Utility.sendRequest(authUri, "PUT", authRequest);

    // If the consumer Cloud is not authorized an error is returned
    if (!authResponse.readEntity(InterCloudAuthResponse.class).isAuthorized()) {
      log.info("ICNProposal: Requester Cloud is UNAUTHORIZED, sending back error");
      ErrorMessage errorMessage = new ErrorMessage("Requester Cloud is UNAUTHORIZED to consume this service, ICNProposal failed.", 401,
                                                   AuthenticationException.class.toString());
      return Response.status(Status.UNAUTHORIZED).entity(errorMessage).build();
    }
    // If it is authorized, send a ServiceRequestForm to the Orchestrator and return the OrchestrationResponse
    Map<String, Boolean> orchestrationFlags = icnProposal.getNegotiationFlags();
    List<PreferredProvider> preferredProviders = new ArrayList<>();
    for (ArrowheadSystem preferredSystem : icnProposal.getPreferredSystems()) {
      preferredProviders.add(new PreferredProvider(preferredSystem, null));
    }

    ServiceRequestForm serviceRequestForm = new ServiceRequestForm.Builder(icnProposal.getRequesterSystem())
        .requesterCloud(icnProposal.getRequesterCloud()).requestedService(icnProposal.getRequestedService()).orchestrationFlags(orchestrationFlags)
        .preferredProviders(preferredProviders).build();
    String orchestratorUri = Utility.getOrchestratorUri();
    orchestratorUri = UriBuilder.fromPath(orchestratorUri).toString();

    Response response = Utility.sendRequest(orchestratorUri, "POST", serviceRequestForm);
    OrchestrationResponse orchResponse = response.readEntity(OrchestrationResponse.class);
    //If the gateway service is not requested, then return the full orchestration response
    if (!icnProposal.getNegotiationFlags().get("useGateway")) {
      ICNResult icnResult = new ICNResult(orchResponse);
      log.info("ICNProposal: returning the OrchestrationResponse to the requester Cloud.");
      return Response.status(response.getStatus()).entity(icnResult).build();
    }

    // Getting the list of preferred brokers from database
    List<Broker> preferredBrokers = dm.getAll(Broker.class, null);
    // Filtering common brokers
    List<Broker> commonBrokers = new ArrayList<>(icnProposal.getPreferredBrokers());
    commonBrokers.retainAll(preferredBrokers);

    // Compiling the gateway request payload
    String gatewayURI = Utility.getGatewayUri();
    gatewayURI = UriBuilder.fromPath(gatewayURI).path("connectToProvider").toString();

    ArrowheadSystem provider = orchResponse.getResponse().get(0).getProvider();
    Map<String, String> metadata = orchResponse.getResponse().get(0).getService().getServiceMetadata();
    boolean isSecure = metadata.containsKey("security") && !metadata.get("security").equals("none");
    int timeout = icnProposal.getTimeout() > GatekeeperMain.timeout ? GatekeeperMain.timeout : icnProposal.getTimeout();
    ConnectToProviderRequest connectionRequest = new ConnectToProviderRequest(commonBrokers.get(0).getAddress(), commonBrokers.get(0).getPort(),
                                                                              provider, isSecure, timeout);
    // Sending request, parsing response
    Response gatewayResponse = Utility.sendRequest(gatewayURI, "PUT", connectionRequest);
    ConnectToProviderResponse connectToProviderResponse = gatewayResponse.readEntity(ConnectToProviderResponse.class);

    GatewayConnectionInfo gatewayConnectionInfo = new GatewayConnectionInfo(commonBrokers.get(0).getAddress(), commonBrokers.get(0).getPort(),
                                                                            connectToProviderResponse.getQueueName(),
                                                                            connectToProviderResponse.getControlQueueName(),
                                                                            Utility.getCoreSystem("gateway").getAuthenticationInfo());
    ICNEnd icnEnd = new ICNEnd(orchResponse.getResponse().get(0), gatewayConnectionInfo);
    log.info("ICNProposal: returning the first OrchestrationForm and the GatewayConnectionInfo to the requester Cloud.");
    return Response.status(response.getStatus()).entity(icnEnd).build();
  }

}
