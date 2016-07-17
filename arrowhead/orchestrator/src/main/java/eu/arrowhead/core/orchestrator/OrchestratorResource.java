package eu.arrowhead.core.orchestrator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import eu.arrowhead.common.Utility;
import eu.arrowhead.common.configuration.SysConfig;
import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.messages.InterCloudAuthRequest;
import eu.arrowhead.common.model.messages.OrchestrationResponse;
import eu.arrowhead.common.model.messages.ServiceRequestForm;

@Path("orchestration")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OrchestratorResource {

	private OrchestratorService service;
	private static Logger log = Logger.getLogger(OrchestratorResource.class.getName());
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response getOrchestration() {
		log.info("Orchestrator cannot be reached through GET methods.");
		return Response.status(Status.BAD_REQUEST).build();
	}
	
	@GET
	@Path("/example")
	public Response exampleSRF(){
		
		ArrowheadSystem requesterSystem;
		ArrowheadService requestedService;
		String requestedQoS;
		Map<String, Boolean> orchestrationFlags = new HashMap<String, Boolean>();
		List<ArrowheadCloud> preferredClouds = new ArrayList<ArrowheadCloud>();
		List<ArrowheadSystem> preferredProviders = new ArrayList<ArrowheadSystem>();
		
		requesterSystem = new ArrowheadSystem("Aitia", "1", "192.168.1.1", "8080", "nincs");
		requestedService = new ArrowheadService(null, null, null, null);
		requestedQoS = "not bad";
		ServiceRequestForm srf = new ServiceRequestForm(requesterSystem, requestedService, requestedQoS, null, null);
		Response resp = Response.status(Status.OK).entity(srf).build();
		return resp;
	}
	
	@POST
	public Response postOrchestration(@Context UriInfo uriInfo, ServiceRequestForm serviceRequestForm){
		OrchestrationResponse or;
		System.out.println("Received the Orchestration request.");
		service = new OrchestratorService(serviceRequestForm);
		if (serviceRequestForm.getRequestedService().isValid()==false){
			System.out.println("Legacy mode orchestration initialized.");
			or = service.legacyModeOrchestration();
			return Response.status(Status.OK).entity(or).build();
		}
		if (serviceRequestForm.getOrchestrationFlags().get("triggerInterCloud") == true){
			System.out.println("Normal Orchestration: Inter Cloud matchmaking is enabled");
			or = service.triggerInterCloud();
			return Response.status(Status.OK).entity(or).build();
		}
		if (serviceRequestForm.getOrchestrationFlags().get("externalServiceRequest") == true){
			System.out.println("The received ServiceRequestForm indicates this is an external request.");
			or = service.externalRequest();
			return Response.status(Status.OK).entity(or).build();
		}
		if (serviceRequestForm.getOrchestrationFlags().get("overrideStore") == false){
			System.out.println("Regular Orchestration process, the overrideStore flag is false.");
			or = service.overrideStoreNotSet();
			return Response.status(Status.OK).entity(or).build();
		}
		System.out.println("Regular Orchestration process, the overrideStore flag is true.");
		or = service.regularOrchestration();
		return Response.status(Status.OK).entity(or).build();
	}

	//Utility class example to send requests
	@PUT
	@Path("test")
	public Response sendingExample(InterCloudAuthRequest request){
		//Getting the base URI
		String URI = SysConfig.getAuthorizationURI();
		
		//Setting the full URI for the request
		URI = UriBuilder.fromPath(URI).path("intercloud").toString();
		
		//Sending request. Parameters: (String) URI, (String) httpMethodType, (Generic) payload
		return Utility.sendRequest(URI, "PUT", request);
		
		//Response respone = Utility.sendRequest(URI, "PUT", request);
		//InterCloudAuthResponse response = response.readEntity(InterCloudAuthResponse.class);
		//more processing...
	}
	
}
