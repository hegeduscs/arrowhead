package eu.arrowhead.core.orchestrator;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.log4j.Logger;

import eu.arrowhead.common.Utility;
import eu.arrowhead.common.configuration.SysConfig;
import eu.arrowhead.common.model.messages.InterCloudAuthRequest;

@Path("")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OrchestratorResource {

	private static Logger log = Logger.getLogger(OrchestratorResource.class.getName());
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getIt() {
		return "Got it";
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
