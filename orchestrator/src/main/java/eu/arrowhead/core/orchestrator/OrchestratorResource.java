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
      log.info("OrchestratorResource:orchestrationProcess throws BadPayloadException");
      throw new BadPayloadException("Bad payload: service request form has missing/incomplete mandatory fields. Some fields are only mandatory when"
                                        + " certain orchestration flags are set to true.");
    }

    OrchestrationResponse orchResponse;
    if (srf.getOrchestrationFlags().get("externalServiceRequest")) {
      log.info("Received an externalServiceRequest.");
      orchResponse = OrchestratorService.externalServiceRequest(srf);
      log.info("externalServiceRequest orchestration returned with " + orchResponse.getResponse().size() + " orchestration forms.");
    } else if (srf.getOrchestrationFlags().get("triggerInterCloud")) {
      log.info("Received a triggerInterCloud request.");
      orchResponse = OrchestratorService.triggerInterCloud(srf);
      log.info("triggerInterCloud orchestration returned with " + orchResponse.getResponse().size() + " orchestration forms.");
    } else if (!srf.getOrchestrationFlags().get("overrideStore")) { //overrideStore == false
      log.info("Received an orchestrationFromStore request.");
      orchResponse = OrchestratorService.orchestrationFromStore(srf);
      log.info("orchestrationFromStore returned with " + orchResponse.getResponse().size() + " orchestration forms.");
    } else {
      log.info("Received a dynamicOrchestration request.");
      orchResponse = OrchestratorService.dynamicOrchestration(srf);
      log.info("regularOrchestration returned with " + orchResponse.getResponse().size() + " orchestration forms.");
    }

    return Response.status(Status.OK).entity(orchResponse).build();
  }

}
