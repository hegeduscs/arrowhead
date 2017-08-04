package eu.arrowhead.core.gatekeeper;

import eu.arrowhead.common.Utility;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.UnavailableServerException;
import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.messages.GSDAnswer;
import eu.arrowhead.common.model.messages.GSDPoll;
import eu.arrowhead.common.model.messages.GSDRequestForm;
import eu.arrowhead.common.model.messages.GSDResult;
import eu.arrowhead.common.model.messages.ICNEnd;
import eu.arrowhead.common.model.messages.ICNProposal;
import eu.arrowhead.common.model.messages.ICNRequestForm;
import eu.arrowhead.common.model.messages.ICNResult;
import eu.arrowhead.common.model.messages.InterCloudAuthRequest;
import eu.arrowhead.common.model.messages.InterCloudAuthResponse;
import eu.arrowhead.common.model.messages.OrchestrationResponse;
import eu.arrowhead.common.model.messages.ServiceQueryForm;
import eu.arrowhead.common.model.messages.ServiceQueryResult;
import eu.arrowhead.common.model.messages.ServiceRequestForm;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    return "This is the Gatekeeper Resource. " + "REST methods: init_gsd, gsd_poll, init_icn, icn_proposal.";
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
    log.info("Entered the GSDRequest method.");

    if (!requestForm.isPayloadUsable()) {
      log.info("Payload is not usable. (GatekeeperResource:GSDRequest BadPayloadException)");
      throw new BadPayloadException(
          "Bad payload: missing/incomplete requestedService." + "Mandatory fields: serviceGroup, serviceDefinition, interfaces.");
    }

    ArrowheadCloud ownCloud = Utility.getOwnCloud();
    log.info("Own cloud info acquired");
    GSDPoll gsdPoll = new GSDPoll(requestForm.getRequestedService(), ownCloud);

    // If no preferred Clouds were given, send GSD poll requests to the
    // neighbor Clouds
    List<String> cloudURIs = new ArrayList<>();
    if (requestForm.getSearchPerimeter() == null || requestForm.getSearchPerimeter().isEmpty()) {
      cloudURIs = Utility.getNeighborCloudURIs();
      log.info(cloudURIs.size() + " NeighborCloud URI(s) acquired.");
    }
    // If there are preferred Clouds given, send GSD poll requests there
    else {
      /*
       * Using a Set removes duplicate entries (which are needed for the
			 * Orchestrator) from the Cloud list.
			 */
      Set<ArrowheadCloud> preferredClouds = new LinkedHashSet<>(requestForm.getSearchPerimeter());
      String URI = null;
      for (ArrowheadCloud cloud : preferredClouds) {
        try {
          URI = Utility.getURI(cloud.getAddress(), Integer.valueOf(cloud.getPort()), cloud.getGatekeeperServiceURI(), false);
        }
        // We skip the clouds with missing information
        catch (NullPointerException ex) {
          continue;
        }
        cloudURIs.add(URI);
        log.info(cloudURIs.size() + " preferred cloud URI(s) acquired.");
      }
    }

    // Finalizing the URIs, process the responses
    List<GSDAnswer> gsdAnswerList = new ArrayList<>();
    Response response = null;
    for (String URI : cloudURIs) {
      URI = UriBuilder.fromPath(URI).path("gsd_poll").toString();
      try {
        response = Utility.sendRequest(URI, "PUT", gsdPoll);
      }
      // We skip the offline gatekeepers
      catch (UnavailableServerException ex) {
        continue;
      }
      log.info("Sent GSD Poll request to: " + URI);
      GSDAnswer gsdAnswer = response.readEntity(GSDAnswer.class);
      if (gsdAnswer != null) {
        log.info("A Cloud " + gsdAnswer.getProviderCloud().toString() + " responded to GSD Poll positively");
        gsdAnswerList.add(gsdAnswer);
      }
    }

    log.info("Sending GSD Poll results to Orchestrator.");
    GSDResult gsdResult = new GSDResult(gsdAnswerList);
    return Response.status(response.getStatus()).entity(gsdResult).build();
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
    log.info("Entered the GSDPoll method. Gatekeeper received a GSD poll from: " + gsdPoll.getRequesterCloud().toString());

    // Polling the Authorization System about the consumer Cloud
    ArrowheadCloud cloud = gsdPoll.getRequesterCloud();
    ArrowheadService service = gsdPoll.getRequestedService();
    InterCloudAuthRequest authRequest = new InterCloudAuthRequest(cloud, service, false);
    String authURI = Utility.getAuthorizationURI();
    authURI = UriBuilder.fromPath(authURI).path("intercloud").toString();
    Response authResponse = Utility.sendRequest(authURI, "PUT", authRequest);
    log.info("Authorization System queried for requester Cloud: " + gsdPoll.getRequesterCloud().toString());

    // If the consumer Cloud is not authorized null is returned
    if (!authResponse.readEntity(InterCloudAuthResponse.class).isAuthorized()) {
      log.info("Requester Cloud is UNAUTHORIZED");
      return Response.status(Status.UNAUTHORIZED).entity(null).build();
    }

    // If it is authorized, poll the Service Registry for the requested
    // Service
    else {
      log.info("Requester Cloud is AUTHORIZED");

      // Compiling the URI and the request payload
      String srURI = Utility.getServiceRegistryURI();
      srURI = UriBuilder.fromPath(srURI).path(service.getServiceGroup()).path(service.getServiceDefinition()).toString();
      String tsig_key = Utility.getCoreSystem("serviceregistry").getAuthenticationInfo();
      ServiceQueryForm queryForm = new ServiceQueryForm(service.getServiceMetadata(), service.getInterfaces(), false, false, tsig_key);

      // Sending back provider Cloud information if the SR poll has
      // results
      Response srResponse = Utility.sendRequest(srURI, "PUT", queryForm);
      log.info("ServiceRegistry queried for requested Service: " + service.toString());
      ServiceQueryResult result = srResponse.readEntity(ServiceQueryResult.class);
      if (result.isPayloadEmpty()) {
        log.info("ServiceRegistry query came back empty for " + service.toString());
        return Response.noContent().entity(null).build();
      }

      log.info("Sending back GSD answer to requester Cloud.");
      GSDAnswer answer = new GSDAnswer(service, Utility.getOwnCloud());
      return Response.ok().entity(answer).build();
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
    log.info("Entered the ICNRequest method.");

    if (!requestForm.isPayloadUsable()) {
      log.info("GatekeeperResource:ICNRequest BadPayloadException");
      throw new BadPayloadException("Bad payload: missing/incomplete ICNRequestForm.");
    }

    // Compiling the payload and then getting the URI
    log.info("Compiling ICN proposal");
    ICNProposal icnProposal = new ICNProposal(requestForm.getRequestedService(), requestForm.getAuthenticationInfo(), Utility.getOwnCloud(),
                                              requestForm.getRequesterSystem(), requestForm.getPreferredProviders(),
                                              requestForm.getNegotiationFlags());

    String icnURI = Utility.getURI(requestForm.getTargetCloud().getAddress(), Integer.valueOf(requestForm.getTargetCloud().getPort()),
                                   requestForm.getTargetCloud().getGatekeeperServiceURI(), false);
    icnURI = UriBuilder.fromPath(icnURI).path("icn_proposal").toString();

    // Sending the the request and then parsing the result
    log.info("Sending ICN proposal to provider Cloud: " + icnURI);
    Response response = Utility.sendRequest(icnURI, "PUT", icnProposal);
    ICNResult result = new ICNResult(response.readEntity(ICNEnd.class));

    log.info("Returning ICN result to Orchestrator.");
    return Response.status(response.getStatus()).entity(result).build();
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
    log.info("Entered the ICNProposal method. Gatekeeper received an ICN proposal from: " + icnProposal.getRequesterCloud().toString());

    // Polling the Authorization System about the consumer Cloud
    ArrowheadCloud cloud = icnProposal.getRequesterCloud();
    ArrowheadService service = icnProposal.getRequestedService();
    InterCloudAuthRequest authRequest = new InterCloudAuthRequest(cloud, service, false);

    String authURI = Utility.getAuthorizationURI();
    authURI = UriBuilder.fromPath(authURI).path("intercloud").toString();
    Response authResponse = Utility.sendRequest(authURI, "PUT", authRequest);
    log.info("Authorization System queried for requester Cloud: " + cloud.toString());

    // If the consumer Cloud is not authorized null is returned
    if (!authResponse.readEntity(InterCloudAuthResponse.class).isAuthorized()) {
      log.info("Requester Cloud is UNAUTHORIZED");
      return Response.status(Status.UNAUTHORIZED).entity(null).build();
    }

		/*
     * If it is authorized, send a ServiceRequestForm to the Orchestrator
		 * and return the OrchestrationResponse
		 */
    else {
      log.info("Requester Cloud is AUTHORIZED");

      Map<String, Boolean> orchestrationFlags = new HashMap<>();
      orchestrationFlags.put("triggerInterCloud", false);
      orchestrationFlags.put("externalServiceRequest", true);
      orchestrationFlags.put("enableInterCloud", false);
      orchestrationFlags.put("metadataSearch", icnProposal.getNegotiationFlags().get("metadataSearch"));
      orchestrationFlags.put("pingProviders", icnProposal.getNegotiationFlags().get("pingProviders"));
      orchestrationFlags.put("overrideStore", false);
      orchestrationFlags.put("storeOnlyActive", false);
      orchestrationFlags.put("matchmaking", false);
      orchestrationFlags.put("onlyPreferred", icnProposal.getNegotiationFlags().get("onlyPreferred"));

      ServiceRequestForm serviceRequestForm = new ServiceRequestForm(icnProposal.getRequesterSystem(), icnProposal.getRequesterCloud(), service,
                                                                     orchestrationFlags, icnProposal.getPreferredProviders(), null, null, null);
      String orchestratorURI = Utility.getOrchestratorURI();
      orchestratorURI = UriBuilder.fromPath(orchestratorURI).path("orchestration").toString();

      log.info("Sending ServiceRequestForm to the Orchestrator. URI: " + orchestratorURI);
      Response response = Utility.sendRequest(orchestratorURI, "POST", serviceRequestForm);
      OrchestrationResponse orchResponse = response.readEntity(OrchestrationResponse.class);

      log.info("Returning the OrchestrationResponse to the requester Cloud.");
      return Response.status(response.getStatus()).entity(new ICNEnd(orchResponse)).build();
    }
  }

}
