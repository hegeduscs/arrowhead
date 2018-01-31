/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.orchestrator.support;

import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.json.support.OrchestrationResponseSupport;
import eu.arrowhead.common.json.support.ServiceRequestFormSupport;
import eu.arrowhead.common.messages.OrchestrationResponse;
import eu.arrowhead.common.messages.ServiceRequestForm;
import eu.arrowhead.core.orchestrator.OrchestratorResource;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("orchestrator/orchestration/support")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OldOrchResource {

  @POST
  public Response supportOrchestrationProcess(ServiceRequestFormSupport srfSupport, @Context ContainerRequestContext requestContext) {
    ArrowheadSystem system = new ArrowheadSystem(srfSupport.getRequesterSystem());
    ArrowheadService service = new ArrowheadService(srfSupport.getRequestedService());
    ServiceRequestForm srf = new ServiceRequestForm.Builder(system).requesterCloud(srfSupport.getRequesterCloud()).requestedService(service).orchestrationFlags(srfSupport.getOrchestrationFlags()).preferredProviders(srfSupport.getPreferredProviders()).build();

    OrchestratorResource orchResource = new OrchestratorResource();
    Response response = orchResource.orchestrationProcess(srf, requestContext);
    OrchestrationResponseSupport orchResponseSupport = new OrchestrationResponseSupport((OrchestrationResponse) response.getEntity());
    return Response.status(response.getStatus()).entity(orchResponseSupport).build();
  }

}
