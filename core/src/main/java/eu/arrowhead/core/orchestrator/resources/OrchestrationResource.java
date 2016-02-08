package eu.arrowhead.core.orchestrator.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import eu.arrowhead.common.model.orchestrator.QueryResult;
import eu.arrowhead.common.model.orchestrator.ServiceQueryForm;
import eu.arrowhead.common.model.orchestrator.ServiceRequestForm;
import eu.arrowhead.core.orchestrator.services.DummySRImitator;
import eu.arrowhead.core.orchestrator.services.OrchestrationService;

@Path("orchestration")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OrchestrationResource {

	OrchestrationService orchestrationService = new OrchestrationService();
	DummySRImitator imitator = new DummySRImitator();

	@GET
	@Path("/example")
	public ServiceRequestForm getIt() {
		return orchestrationService.getExample();
	}

	@GET
	public ServiceRequestForm getSRE() {
		return orchestrationService.getCurrentForm();
	}

	@PUT
	public QueryResult putSRE(ServiceRequestForm srf) {
		orchestrationService.setCurrentForm(srf);
		return getQueryResult();
	}
	
	public QueryResult getQueryResult(){
		return orchestrationService.sendSQF();		
	}

	/*
	 * Testing communication between core systems
	 */
	@GET
	@Path("/test")
	public String testMethod() {
		Client client = ClientBuilder.newClient();
		Response response = client.target("http://localhost:8080/core/authorization/operator/A/cloud/b").request()
				.get();
		String message = response.readEntity(String.class);
		return message;
	}

	@PUT
	@Path("/SR") // tesztelésre, SR-t imitálja
	public QueryResult returnResult(ServiceQueryForm sqf) {
		return imitator.getResult(sqf);
	}

	@GET
	@Path("/SRexample")
	public ServiceQueryForm getsqfexample() {
		return imitator.getExample();
	}
}
