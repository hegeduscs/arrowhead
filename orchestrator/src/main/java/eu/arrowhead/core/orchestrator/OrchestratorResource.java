package eu.arrowhead.core.orchestrator;

import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.model.messages.OrchestrationResponse;
import eu.arrowhead.common.model.messages.ServiceRequestForm;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.log4j.Logger;

/**
 * TODO javadoc for the class
 * This is the REST resource for the Orchestrator Core System.
 */
@Path("orchestration")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OrchestratorResource {

  private static Logger log = Logger.getLogger(OrchestratorResource.class.getName());

  /*
   * Simple test method to see if the http server where this resource is registered works or not.
   */
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String getIt() {
    return "Got it!";
  }

  /**
   * This method initiates the correct orchestration process determined by orchestration flags in the service request form. The returned response
   * (can) consists a list of endpoints where the requester System can consume the requested Service.
   *
   * @return OrchestrationResponse
   */

  //TODO sanity check: requesterSystem és a cert common name harmóniában vannak
  @POST
  public Response orchestrationProcess(ServiceRequestForm srf) {
    if (!srf.isValid()) {
      log.error("orchestrationProcess BadPayloadException");
      throw new BadPayloadException("Bad payload: service request form has missing/incomplete mandatory fields. See the documentation of "
                                        + "ServiceRequestForm for more details.");
    }

    OrchestrationResponse orchResponse;
    if (srf.getOrchestrationFlags().get("externalServiceRequest")) {
      log.info("Received an externalServiceRequest.");
      orchResponse = OrchestratorService.externalServiceRequest(srf);
    } else if (srf.getOrchestrationFlags().get("triggerInterCloud")) {
      log.info("Received a triggerInterCloud request.");
      orchResponse = OrchestratorService.triggerInterCloud(srf);
    } else if (!srf.getOrchestrationFlags().get("overrideStore")) { //overrideStore == false
      log.info("Received an orchestrationFromStore request.");
      orchResponse = OrchestratorService.orchestrationFromStore(srf);
    } else {
      log.info("Received a dynamicOrchestration request.");
      orchResponse = OrchestratorService.dynamicOrchestration(srf);
    }

    log.info("The orchestration process returned with " + orchResponse.getResponse().size() + " orchestration forms.");
    return Response.status(Status.OK).entity(orchResponse).build();
  }

}
