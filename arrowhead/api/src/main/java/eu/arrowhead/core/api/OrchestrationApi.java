package eu.arrowhead.core.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import eu.arrowhead.common.configuration.DatabaseManager;
import eu.arrowhead.common.database.OrchestrationStore;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.messages.StorePayload;

@Path("orchestration")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrchestrationApi {

	DatabaseManager dm = DatabaseManager.getInstance();
	HashMap<String, Object> restrictionMap = new HashMap<String, Object>();
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getIt() {
		return "Got it";
	}
	
	/**
	 * Returns the Orchestration Store entries from the database specified by
	 * the consumer and/or the service. If the payload is empty ({}), all entries
	 * will be returned.
	 * 
	 * @param {StorePayload}
	 *            payload
	 * @return List<OrchestrationStore>
	 */
	@PUT
	@Path("/store")
	public List<OrchestrationStore> getStoreEntries(StorePayload payload) {
		
		List<OrchestrationStore> store = new ArrayList<OrchestrationStore>();
		HashMap<String, Object> rm = new HashMap<String, Object>();
		if (payload.getConsumer() != null) {
			rm.put("systemGroup", payload.getConsumer().getSystemGroup());
			rm.put("systemName", payload.getConsumer().getSystemName());
			ArrowheadSystem consumer = dm.get(ArrowheadSystem.class, rm);
			restrictionMap.put("consumer", consumer);
		}
		
		rm.clear();
		
		if (payload.getService() != null) {
			rm.put("serviceGroup", payload.getService().getServiceGroup());
			rm.put("serviceDefinition", payload.getService().getServiceDefinition());
			ArrowheadService service = dm.get(ArrowheadService.class, rm);
			restrictionMap.put("service", service);
		}
		
		store = dm.getAll(OrchestrationStore.class, restrictionMap);

		return store;
	}

	/**
	 * Adds a list of Orchestration Store entries to the database. Elements
	 * which would cause DuplicateEntryException are being skipped. The returned
	 * list only contains the elements which was saved in the process.
	 *
	 * @param {List<OrchestrationStore>}
	 *            serviceList
	 * @return List<OrchestrationStore>
	 * @throws DataNotFoundException 
	 */
	@POST
	@Path("/store")
	public List<OrchestrationStore> addStoreEntries(List<OrchestrationStore> storeEntries) {
		List<OrchestrationStore> store = new ArrayList<OrchestrationStore>();
		for (OrchestrationStore entry : storeEntries) {
			restrictionMap.clear();
			restrictionMap.put("serviceGroup", entry.getService().getServiceGroup());
			restrictionMap.put("serviceDefinition", entry.getService().getServiceDefinition());
			ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);
			if (service == null) {
				throw new DataNotFoundException("Invalid Orchestration Store entry: missing ArrowheadService.");
			}
			restrictionMap.clear();
			restrictionMap.put("systemGroup", entry.getConsumer().getSystemGroup());
			restrictionMap.put("systemName", entry.getConsumer().getSystemName());
			ArrowheadSystem consumer = dm.get(ArrowheadSystem.class, restrictionMap);
			if (consumer == null) {
				throw new DataNotFoundException("Invalid Orchestration Store entry: "
						+ "missing consumer ArrowheadSystem.");
			}
			
			ArrowheadSystem providerSystem = null;
			if(entry.getProviderSystem() != null){
				restrictionMap.clear();
				restrictionMap.put("systemGroup", entry.getProviderSystem().getSystemGroup());
				restrictionMap.put("systemName", entry.getProviderSystem().getSystemName());
				providerSystem = dm.get(ArrowheadSystem.class, restrictionMap);
			}
			
			ArrowheadCloud providerCloud = null;
			if(entry.getProviderCloud() != null){
				restrictionMap.clear();
				restrictionMap.put("operator", entry.getProviderCloud().getOperator());
				restrictionMap.put("cloudName", entry.getProviderCloud().getCloudName());
				providerCloud = dm.get(ArrowheadCloud.class, restrictionMap);
			}
			
			restrictionMap.clear();
			restrictionMap.put("consumer", consumer);
			restrictionMap.put("service", service);
			OrchestrationStore retrievedEntry = dm.get(OrchestrationStore.class, restrictionMap);
			if (retrievedEntry == null) {
				entry.setConsumer(consumer);
				entry.setService(service);
				if(providerSystem != null){
					entry.setProviderSystem(providerSystem);
				}
				if(providerCloud != null){
					entry.setProviderCloud(providerCloud);
				}
				
				dm.merge(entry);
				store.add(entry);
			}
		}

		return store;
	}

	/**
	 * Deletes the Orchestration Store entry with the serial number specified by
	 * the path parameter. Returns 200 if the delete is succesful, 204 (no
	 * content) if the entry was not in the database to begin with.
	 * 
	 * @param {int}
	 *            serial
	 */
	@DELETE
	@Path("/store/{serial}")
	public Response deleteEntry(@PathParam("serial") int serial) {
		restrictionMap.put("serialNumber", serial);
		OrchestrationStore entry = dm.get(OrchestrationStore.class, restrictionMap);
		if (entry == null) {
			return Response.noContent().build();
		} else {
			dm.delete(entry);
			return Response.ok().build();
		}
	}

	
}
