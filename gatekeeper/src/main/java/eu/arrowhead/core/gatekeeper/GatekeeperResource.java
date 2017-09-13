package eu.arrowhead.core.gatekeeper;

import eu.arrowhead.common.Utility;
import eu.arrowhead.common.database.ArrowheadCloud;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.exception.AuthenticationException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.exception.ErrorMessage;
import eu.arrowhead.common.messages.GSDAnswer;
import eu.arrowhead.common.messages.GSDPoll;
import eu.arrowhead.common.messages.GSDRequestForm;
import eu.arrowhead.common.messages.GSDResult;
import eu.arrowhead.common.messages.ICNProposal;
import eu.arrowhead.common.messages.ICNRequestForm;
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

/**
 * This is the REST resource for the Gatekeeper Core System.
 */
@Path("gatekeeper")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GatekeeperResource {

  private static Logger log = Logger.getLogger(GatekeeperResource.class.getName());

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String getIt() {
    return "This is the Gatekeeper Resource. Offered resources at: init_gsd, gsd_poll, init_icn, icn_proposal.";
  }

  /**
   * This function represents the consumer-side of GlobalServiceDiscovery, where the GateKeeper of the consumer System tries to find a provider Cloud
   * for the requested Service.
   *
   * @return GSDResult
   */
  @PUT
  @Path("init_gsd")
  public Response GSDRequest(GSDRequestForm requestForm) {
    if (!requestForm.isValid()) {
      log.error("GSDRequest BadPayloadException");
      throw new BadPayloadException("init_gsd received bad payload: requestedService is missing or it is not valid.");
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
          //TODO gatekeeper szétszedése lásd arrowhead_todo doksi/új ötletek része
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
        response = Utility.sendRequest(uri, "PUT", gsdPoll);
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
    InterCloudAuthRequest authRequest = new InterCloudAuthRequest(gsdPoll.getRequesterCloud(), gsdPoll.getRequestedService(), false);
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
   * This function represents the consumer-side of InterCloudNegotiations, where the Gatekeeper sends information about the requester System. (SSL
   * secured)
   *
   * @return ICNResult
   */
  @PUT
  @Path("init_icn")
  public Response ICNRequest(ICNRequestForm requestForm) {
    if (!requestForm.isValid()) {
      log.error("ICNRequest BadPayloadException");
      throw new BadPayloadException("init_icn received bad payload: missing/incomplete ICNRequestForm.");
    }

    // Compiling the payload and then getting the URI
    ICNProposal icnProposal = new ICNProposal(requestForm.getRequestedService(), Utility.getOwnCloud(), requestForm.getRequesterSystem(),
                                              requestForm.getPreferredSystems(), requestForm.getNegotiationFlags(),
                                              requestForm.getAuthenticationInfo());

    String icnUri = Utility.getUri(requestForm.getTargetCloud().getAddress(), requestForm.getTargetCloud().getPort(),
                                   requestForm.getTargetCloud().getGatekeeperServiceURI(), false);
    icnUri = UriBuilder.fromPath(icnUri).path("icn_proposal").toString();

    // Sending the the request and then parsing the result
    Response response = Utility.sendRequest(icnUri, "PUT", icnProposal);
    ICNResult result = response.readEntity(ICNResult.class);

    log.info("ICNRequest: returning ICNResult to Orchestrator.");
    return Response.status(Status.OK).entity(result).build();
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
    InterCloudAuthRequest authRequest = new InterCloudAuthRequest(icnProposal.getRequesterCloud(), icnProposal.getRequestedService(), false);
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
    else {
      Map<String, Boolean> orchestrationFlags = icnProposal.getNegotiationFlags();
      orchestrationFlags.put("externalServiceRequest", true);
      List<PreferredProvider> preferredProviders = new ArrayList<>();
      for (ArrowheadSystem preferredSystem : icnProposal.getPreferredSystems()) {
        preferredProviders.add(new PreferredProvider(preferredSystem, null));
      }

      ServiceRequestForm serviceRequestForm = new ServiceRequestForm.Builder(icnProposal.getRequesterSystem())
          .requesterCloud(icnProposal.getRequesterCloud()).requestedService(icnProposal.getRequestedService()).orchestrationFlags(orchestrationFlags)
          .preferredProviders(preferredProviders).build();
      String orchestratorUri = Utility.getOrchestratorUri();
      orchestratorUri = UriBuilder.fromPath(orchestratorUri).path("orchestration").toString();

      Response response = Utility.sendRequest(orchestratorUri, "POST", serviceRequestForm);
      OrchestrationResponse orchResponse = response.readEntity(OrchestrationResponse.class);

      log.info("ICNProposal: returning the OrchestrationResponse to the requester Cloud.");
      return Response.status(Status.OK).entity(new ICNResult(orchResponse)).build();
    }
  }

}
