package eu.arrowhead.core.orchestrator.store;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import eu.arrowhead.common.configuration.DatabaseManager;
import eu.arrowhead.common.configuration.SysConfig;
import eu.arrowhead.common.database.OrchestrationStore;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.model.messages.OrchestrationStoreQuery;
import eu.arrowhead.common.model.messages.StorePayload;

/**
 * @author umlaufz
 *
 */
@Path("")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StoreResource {
	
	DatabaseManager dm = DatabaseManager.getInstance();
	SysConfig sysConfig = new SysConfig();
	HashMap<String, Object> restrictionMap = new HashMap<String, Object>();
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getIt() {
		return "Got it";
	}
	/*
	 * get (Client) client = consumer
		get (Client, Service)
		getActive (Client)
	 */
	
	/**
	 * todo
	 * 
	 * @param query
	 * @return
	 */
	@PUT
	public Response storeQuery(OrchestrationStoreQuery query){
		
		if(!query.isPayloadUsable()){
			throw new BadPayloadException("Bad payload: missing/incomplete system or service"
					+ "in the query payload.");
		}
		//store service-be áthozni apiból a függvényt ide
		StorePayload payload = new StorePayload(query.getRequesterSystem(), 
				query.getRequestedService());
		
		Client client = ClientBuilder.newClient();
		Response response = client.target(sysConfig.getApiURI())
				.request().header("Content-type", "application/json")
				.put(Entity.json(payload));
		
		List<OrchestrationStore> store = new ArrayList<OrchestrationStore>();
		store = response.readEntity(new GenericType<List<OrchestrationStore>>(){});
		
		return null;
	}
	
	
}
