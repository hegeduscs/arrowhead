/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.orchestrator;

import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.messages.OrchestrationResponse;
import eu.arrowhead.common.messages.ServiceRequestForm;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.log4j.Logger;

/**
 * REST resource for the Orchestrator Core System.
 */
@Path("orchestrator/orchestration")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OrchestratorResource {

  private static final Logger log = Logger.getLogger(OrchestratorResource.class.getName());

  /**
   * Simple test method to see if the http server where this resource is registered works or not.
   */

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String getIt() {
    return "Orchestrator got it!";
  }

  /**
   * This method initiates the correct orchestration process determined by orchestration flags in the <tt>ServiceRequestForm</tt>. The returned
   * response (can) consists a list of endpoints where the requester System can consume the requested Service.
   *
   * @return OrchestrationResponse
   */
  @POST
  public Response orchestrationProcess(ServiceRequestForm srf) {
    if (!srf.isValid()) {
      log.error("orchestrationProcess BadPayloadException");
      throw new BadPayloadException("Bad payload: service request form has missing/incomplete mandatory fields.", Status.BAD_REQUEST.getStatusCode());
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

  /**
   * Default Store orchestration process offered on a GET request, where the requester only has to send 2 String path parameters.
   */
  @GET
  @Path("{systemName}")
  public Response storeOrchestrationProcess(@PathParam("systemName") String systemName, @Context HttpServletRequest request) {
    ArrowheadSystem requesterSystem = new ArrowheadSystem(systemName, request.getRemoteAddr(), 0, null);
    log.info("Received a GET Store orchestration from: " + request.getRemoteAddr() + " " + requesterSystem.getSystemName());

    ServiceRequestForm srf = new ServiceRequestForm.Builder(requesterSystem).build();
    OrchestrationResponse orchResponse = OrchestratorService.orchestrationFromStore(srf);

    log.info("Default store orchestration returned with " + orchResponse.getResponse().size() + " orchestration forms.");
    return Response.status(Status.OK).entity(orchResponse).build();
  }

}
