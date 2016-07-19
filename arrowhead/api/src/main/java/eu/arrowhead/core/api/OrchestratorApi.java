package eu.arrowhead.core.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import eu.arrowhead.common.configuration.DatabaseManager;
import eu.arrowhead.common.database.OrchestrationStore;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.messages.OrchestrationStoreQuery;

@Path("orchestrator/store")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrchestratorApi {

	DatabaseManager dm = DatabaseManager.getInstance();
	HashMap<String, Object> restrictionMap = new HashMap<String, Object>();
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getIt() {
		return "Got it";
	}
	
	/**
	 * Returns an Orchestration Store entry from the database specified by the
	 * database generated id.
	 * 
	 * @param int id
	 * @return OrchestrationStore
	 * @throws DataNotFoundException
	 */
	@GET
	@Path("{id}")
	public Response getStoreEntry(@PathParam("id") int id){
		restrictionMap.put("id", id);
		OrchestrationStore entry = dm.get(OrchestrationStore.class, restrictionMap);
		if(entry == null){
			throw new DataNotFoundException("Requested store entry was not found in the database.");
		}
		else{
			return Response.ok(entry).build();
		}
	}
	
	/**
	 * Returns all the entries of the Orchestration Store.
	 * 
	 * @return List<OrchestrationStore>
	 * @throws DataNotFoundException
	 */
	@GET
	@Path("all")
	public List<OrchestrationStore> getAllStoreEntries(){
		List<OrchestrationStore> store = new ArrayList<OrchestrationStore>();
		store = dm.getAll(OrchestrationStore.class, restrictionMap);
		if(store.isEmpty()){
			throw new DataNotFoundException("The Orchestration Store is empty.");
		}
			
		Collections.sort(store);
		return store;
	}
	
	/**
	 * Returns all the active entries of the Orchestration Store.
	 * 
	 * @return List<OrchestrationStore>
	 * @throws DataNotFoundException
	 */
	@GET
	@Path("all_active")
	public List<OrchestrationStore> getActiveStoreEntries(){
		List<OrchestrationStore> store = new ArrayList<OrchestrationStore>();
		restrictionMap.put("isActive", true);
		store = dm.getAll(OrchestrationStore.class, restrictionMap);
		if(store.isEmpty()){
			throw new DataNotFoundException("Active Orchestration Store entries were not found.");
		}
		
		Collections.sort(store);
		return store;
	}
	
	/**
	 * Returns the Orchestration Store entries from the database specified by
	 * the consumer (and the service).
	 * 
	 * @param OrchestrationStoreQuery query
	 * @return List<OrchestrationStore>
	 * @throws BadPayloadException, DataNotFoundException
	 */
	@PUT
	public Response getStoreEntries(OrchestrationStoreQuery query) {
		if(!query.isPayloadUsable()){
			throw new BadPayloadException("Bad payload: mandatory field(s) of requesterSystem "
					+ "is/are missing. (systemGroup, systemName)");
		}
		
		List<OrchestrationStore> store = new ArrayList<OrchestrationStore>();
		HashMap<String, Object> rm = new HashMap<String, Object>();
		
		rm.put("systemGroup", query.getRequesterSystem().getSystemGroup());
		rm.put("systemName", query.getRequesterSystem().getSystemName());
		ArrowheadSystem consumer = dm.get(ArrowheadSystem.class, rm);
		restrictionMap.put("consumer", consumer);
		
		if(query.isOnlyActive()){
			restrictionMap.put("isActive", true);
		}
		if(query.getRequestedService() != null && query.getRequestedService().isValid()){
			rm.clear();
			rm.put("serviceGroup", query.getRequestedService().getServiceGroup());
			rm.put("serviceDefinition", query.getRequestedService().getServiceDefinition());
			ArrowheadService service = dm.get(ArrowheadService.class, rm);
			restrictionMap.put("service", service);
		}
		
		store = dm.getAll(OrchestrationStore.class, restrictionMap);
		if(store.isEmpty()){
			throw new DataNotFoundException("Store entries specified by the payload "
					+ "were not found in the database.");
		}
		
		Collections.sort(store);
		GenericEntity<List<OrchestrationStore>> entity = 
				new GenericEntity<List<OrchestrationStore>>(store) {};
		return Response.ok(entity).build();
	}

	/**
	 * Adds a list of Orchestration Store entries to the database. Elements which would throw
	 * BadPayloadException (caused by missing/incomplete consumer, service or negative priority) 
	 * are being skipped. The returned list only contains the elements which was saved in the process.
	 *
	 * @param List<OrchestrationStore> serviceList
	 * @return List<OrchestrationStore>
	 */
	@POST
	public List<OrchestrationStore> addStoreEntries(List<OrchestrationStore> storeEntries) {
		List<OrchestrationStore> store = new ArrayList<OrchestrationStore>();
		for (OrchestrationStore entry : storeEntries) {
			if(entry.isPayloadUsable()){
				restrictionMap.clear();
				restrictionMap.put("systemGroup", entry.getConsumer().getSystemGroup());
				restrictionMap.put("systemName", entry.getConsumer().getSystemName());
				ArrowheadSystem consumer = dm.get(ArrowheadSystem.class, restrictionMap);
				if (consumer == null) {
					consumer = dm.save(entry.getConsumer());
				}
				
				restrictionMap.clear();
				restrictionMap.put("serviceGroup", entry.getService().getServiceGroup());
				restrictionMap.put("serviceDefinition", entry.getService().getServiceDefinition());
				ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);
				if (service == null) {
					service = dm.save(entry.getService());
				}
				
				ArrowheadSystem providerSystem = null;
				if(entry.getProviderSystem() != null && entry.getProviderSystem().isValid()){
					restrictionMap.clear();
					restrictionMap.put("systemGroup", entry.getProviderSystem().getSystemGroup());
					restrictionMap.put("systemName", entry.getProviderSystem().getSystemName());
					providerSystem = dm.get(ArrowheadSystem.class, restrictionMap);
					if(providerSystem == null){
						providerSystem = dm.save(entry.getProviderSystem());
					}
				}

				ArrowheadCloud providerCloud = null;
				if(entry.getProviderCloud() != null && entry.getProviderCloud().isValid()){
					restrictionMap.clear();
					restrictionMap.put("operator", entry.getProviderCloud().getOperator());
					restrictionMap.put("cloudName", entry.getProviderCloud().getCloudName());
					providerCloud = dm.get(ArrowheadCloud.class, restrictionMap);
					if(providerCloud == null){
						providerCloud = dm.save(entry.getProviderCloud());
					}
				}
				
				entry.setConsumer(consumer);
				entry.setService(service);
				entry.setProviderSystem(providerSystem);	
				entry.setProviderCloud(providerCloud);
				entry.setLastUpdated(new Date());
				dm.merge(entry);
				store.add(entry);
			}
		}

		return store;
	}
	
	/**
	 * Toggles the isActive boolean for the Orchestration Store entry specified by the id field.
	 * 
	 * @param int id
	 * @return OrchestrationStore
	 * @throws DataNotFoundException
	 */
	@GET
	@Path("active/{id}")
	public Response toggleIsActive(@PathParam("id") int id){
		restrictionMap.put("id", id);
		OrchestrationStore entry = dm.get(OrchestrationStore.class, restrictionMap);
		if(entry == null){
			throw new DataNotFoundException("Orchestration Store entry with this id "
					+ "was not found in the database.");
		}
		else{
			entry.setActive(!entry.isActive());
			dm.merge(entry);
			return Response.ok(entry).build();
		}
	}
	
	/**
	 * Updates an Orchestration Store entry specified by the id field of the payload.
	 * 
	 * @param OrchestrationStore payload
	 * @return OrchestrationStore
	 * @throws BadPayloadException, DataNotFoundException
	 */
	@PUT
	@Path("update}")
	public Response updateEntry(OrchestrationStore payload){
		
		if(!payload.isPayloadUsable()){
			throw new BadPayloadException("Bad payload: mandatory field(s) is/are "
					+ "missing from the payload.");
		}
		
		restrictionMap.put("id", payload.getId());
		OrchestrationStore storeEntry = dm.get(OrchestrationStore.class, restrictionMap);
		if(storeEntry == null){
			throw new DataNotFoundException("Store entry specified by the id was "
					+ "not found in the database.");
		}
		else{
			storeEntry.setConsumer(payload.getConsumer());
			storeEntry.setService(payload.getService());
			storeEntry.setProviderCloud(payload.getProviderCloud());
			storeEntry.setProviderSystem(payload.getProviderSystem());
			storeEntry.setPriority(payload.getPriority());
			storeEntry.setActive(payload.isActive());
			storeEntry.setName(payload.getName());
			storeEntry.setLastUpdated(new Date());
			storeEntry.setOrchestrationRule(payload.getOrchestrationRule());
			storeEntry = dm.merge(storeEntry);
			
			return Response.status(Status.ACCEPTED).entity(storeEntry).build();
		}
	}

	/**
	 * Deletes the Orchestration Store entry with the name specified by
	 * the path parameter. Returns 200 if the delete is succesful, 204 (no
	 * content) if the entry was not in the database to begin with.
	 * 
	 * @param int id
	 */
	@DELETE
	@Path("{id}")
	public Response deleteEntry(@PathParam("id") String id) {
		restrictionMap.put("id", id);
		OrchestrationStore entry = dm.get(OrchestrationStore.class, restrictionMap);
		if (entry == null) {
			return Response.noContent().build();
		} else {
			dm.delete(entry);
			return Response.ok().build();
		}
	}
	
	/**
	 * Deletes the Orchestration Store entries from the database specified by
	 * the consumer (and the service). Returns 200 if the delete is succesful, 
	 * 204 (no content) if no matching entries were in the database to begin with.
	 * 
	 * @param OrchestrationStoreQuery query
	 */
	@DELETE
	public Response deleteEntries(OrchestrationStoreQuery query){
		if(!query.isPayloadUsable()){
			throw new BadPayloadException("Bad payload: mandatory field(s) of requesterSystem "
					+ "is/are missing. (systemGroup, systemName)");
		}
		
		List<OrchestrationStore> store = new ArrayList<OrchestrationStore>();
		HashMap<String, Object> rm = new HashMap<String, Object>();
		
		rm.put("systemGroup", query.getRequesterSystem().getSystemGroup());
		rm.put("systemName", query.getRequesterSystem().getSystemName());
		ArrowheadSystem consumer = dm.get(ArrowheadSystem.class, rm);
		restrictionMap.put("consumer", consumer);

		if(query.getRequestedService() != null && query.getRequestedService().isValid()){
			rm.clear();
			rm.put("serviceGroup", query.getRequestedService().getServiceGroup());
			rm.put("serviceDefinition", query.getRequestedService().getServiceDefinition());
			ArrowheadService service = dm.get(ArrowheadService.class, rm);
			restrictionMap.put("service", service);
		}
		
		store = dm.getAll(OrchestrationStore.class, restrictionMap);
		if(store.isEmpty()){
			return Response.noContent().build();
		}
		else{
			for(OrchestrationStore entry: store){
				dm.delete(entry);
			}
			return Response.ok().build();
		}
	}

	
}
