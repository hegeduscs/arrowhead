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
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

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

	@Context
	Configuration configuration;
	private static Logger log = Logger.getLogger(OrchestratorApi.class.getName());
	DatabaseManager dm = DatabaseManager.getInstance();
	HashMap<String, Object> restrictionMap = new HashMap<String, Object>();
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getIt(@Context SecurityContext sc) {
		if (sc.isSecure()) {
			log.info("Got a request from a secure channel. Cert: " + sc.getUserPrincipal().getName());
			if(!Main.isClientAuthorized(sc, configuration)){
				//throw new AuthenticationException("This client is not allowed to use this resource.");
				log.info("Unauthorized access! (SSL)");
			}
			else{
				log.info("Identification is successful! (SSL)");
			}
		}
		
		return "Got it";
	}
	
	/**
	 * Returns an Orchestration Store entry from the database specified by the
	 * database generated id.
	 * 
	 * @param Integer id
	 * @return OrchestrationStore
	 * @throws DataNotFoundException
	 */
	@GET
	@Path("{id}")
	public Response getStoreEntry(@Context SecurityContext sc, @PathParam("id") int id){
		if (sc.isSecure()) {
			log.info("Got a request from a secure channel. Cert: " + sc.getUserPrincipal().getName());
			if(!Main.isClientAuthorized(sc, configuration)){
				//throw new AuthenticationException("This client is not allowed to use this resource.");
				log.info("Unauthorized access! (SSL)");
			}
			else{
				log.info("Identification is successful! (SSL)");
			}
		}
		
		restrictionMap.put("id", id);
		OrchestrationStore entry = dm.get(OrchestrationStore.class, restrictionMap);
		if(entry == null){
			log.info("OrchestrationApi:getStoreEntry throws DataNotFoundException");
			throw new DataNotFoundException("Requested store entry was not found in the database.");
		}
		else{
			log.info("getStoreEntry returns a store entry.");
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
	public List<OrchestrationStore> getAllStoreEntries(@Context SecurityContext sc){
		if (sc.isSecure()) {
			log.info("Got a request from a secure channel. Cert: " + sc.getUserPrincipal().getName());
			if(!Main.isClientAuthorized(sc, configuration)){
				//throw new AuthenticationException("This client is not allowed to use this resource.");
				log.info("Unauthorized access! (SSL)");
			}
			else{
				log.info("Identification is successful! (SSL)");
			}
		}
		
		List<OrchestrationStore> store = new ArrayList<OrchestrationStore>();
		store = dm.getAll(OrchestrationStore.class, restrictionMap);
		if(store.isEmpty()){
			log.info("OrchestrationApi:getAllStoreEntries throws DataNotFoundException.");
			throw new DataNotFoundException("The Orchestration Store is empty.");
		}
			
		Collections.sort(store);
		log.info("getAllStoreEntries successfully returns.");
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
	public List<OrchestrationStore> getActiveStoreEntries(@Context SecurityContext sc){
		if (sc.isSecure()) {
			log.info("Got a request from a secure channel. Cert: " + sc.getUserPrincipal().getName());
			if(!Main.isClientAuthorized(sc, configuration)){
				//throw new AuthenticationException("This client is not allowed to use this resource.");
				log.info("Unauthorized access! (SSL)");
			}
			else{
				log.info("Identification is successful! (SSL)");
			}
		}
		
		List<OrchestrationStore> store = new ArrayList<OrchestrationStore>();
		restrictionMap.put("isActive", true);
		store = dm.getAll(OrchestrationStore.class, restrictionMap);
		if(store.isEmpty()){
			log.info("OrchestrationApi:getActiveStoreEntries throws DataNotFoundException.");
			throw new DataNotFoundException("Active Orchestration Store entries were not found.");
		}
		
		Collections.sort(store);
		log.info("getActiveStoreEntries successfully returns.");
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
	public Response getStoreEntries(@Context SecurityContext sc, OrchestrationStoreQuery query) {
		if (sc.isSecure()) {
			log.info("Got a request from a secure channel. Cert: " + sc.getUserPrincipal().getName());
			if(!Main.isClientAuthorized(sc, configuration)){
				//throw new AuthenticationException("This client is not allowed to use this resource.");
				log.info("Unauthorized access! (SSL)");
			}
			else{
				log.info("Identification is successful! (SSL)");
			}
		}
		
		if(!query.isPayloadUsable()){
			log.info("OrchestrationApi:getStoreEntries throws BadPayloadException.");
			throw new BadPayloadException("Bad payload: mandatory field(s) of requesterSystem "
					+ "is/are missing.");
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
		if(query.getRequestedService() != null && query.getRequestedService().isValidSoft()){
			rm.clear();
			rm.put("serviceGroup", query.getRequestedService().getServiceGroup());
			rm.put("serviceDefinition", query.getRequestedService().getServiceDefinition());
			ArrowheadService service = dm.get(ArrowheadService.class, rm);
			restrictionMap.put("service", service);
		}
		
		store = dm.getAll(OrchestrationStore.class, restrictionMap);
		if(store.isEmpty()){
			log.info("OrchestrationApi:getStoreEntries throws DataNotFoundException.");
			throw new DataNotFoundException("Store entries specified by the payload "
					+ "were not found in the database.");
		}
		
		Collections.sort(store);
		GenericEntity<List<OrchestrationStore>> entity = 
				new GenericEntity<List<OrchestrationStore>>(store) {};
		log.info("getStoreEntries successfully returns.");
		return Response.ok(entity).build();
	}

	/**
	 * Adds a list of Orchestration Store entries to the database. Elements which would throw
	 * BadPayloadException are being skipped. The returned list only contains the elements 
	 * which was saved in the process.
	 *
	 * @param List<OrchestrationStore> serviceList
	 * @return List<OrchestrationStore>
	 */
	@POST
	public List<OrchestrationStore> addStoreEntries(@Context SecurityContext sc, List<OrchestrationStore> storeEntries) {
		if (sc.isSecure()) {
			log.info("Got a request from a secure channel. Cert: " + sc.getUserPrincipal().getName());
			if(!Main.isClientAuthorized(sc, configuration)){
				//throw new AuthenticationException("This client is not allowed to use this resource.");
				log.info("Unauthorized access! (SSL)");
			}
			else{
				log.info("Identification is successful! (SSL)");
			}
		}
		
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

				entry.setConsumer(consumer);
				entry.setService(service);
				entry.setProviderSystem(providerSystem);
				entry.setProviderCloud(providerCloud);
				entry.setLastUpdated(new Date());
				dm.merge(entry);
				store.add(entry);
			}
		}
		
		log.info("addStoreEntries successfully returns. Arraylist size: " + store.size());
		return store;
	}
	
	/**
	 * Toggles the isActive boolean for the Orchestration Store entry specified by the id field.
	 * 
	 * @param Integer id
	 * @return OrchestrationStore
	 * @throws DataNotFoundException, BadPayloadException
	 */
	@GET
	@Path("active/{id}")
	public Response toggleIsActive(@Context SecurityContext sc, @PathParam("id") int id){
		if (sc.isSecure()) {
			log.info("Got a request from a secure channel. Cert: " + sc.getUserPrincipal().getName());
			if(!Main.isClientAuthorized(sc, configuration)){
				//throw new AuthenticationException("This client is not allowed to use this resource.");
				log.info("Unauthorized access! (SSL)");
			}
			else{
				log.info("Identification is successful! (SSL)");
			}
		}
		
		restrictionMap.put("id", id);
		OrchestrationStore entry = dm.get(OrchestrationStore.class, restrictionMap);
		if(entry == null){
			log.info("OrchestrationApi:toggleIsActive throws DataNotFoundException.");
			throw new DataNotFoundException("Orchestration Store entry with this id "
					+ "was not found in the database.");
		}
		else if(entry.getProviderCloud() != null){
			log.info("OrchestrationApi:toggleIsActive throws BadPayloadException.");
			throw new BadPayloadException("Only intra-cloud store entries can be active.");
		}
		else{
			entry.setIsActive(!entry.getIsActive());
			dm.merge(entry);
			log.info("toggleIsActive succesfully returns.");
			return Response.ok(entry).build();
		}
	}
	
	/**
	 * Updates the non-entity fields of an Orchestration Store entry specified by the 
	 * id field of the payload. Entity fields have their own update method in CommonApi.class.
	 * (Or delete and then post the modified entry again.)
	 * 
	 * @param OrchestrationStore payload
	 * @return OrchestrationStore
	 * @throws BadPayloadException, DataNotFoundException
	 */
	@PUT
	@Path("update")
	public Response updateEntry(@Context SecurityContext sc, OrchestrationStore payload){
		if (sc.isSecure()) {
			log.info("Got a request from a secure channel. Cert: " + sc.getUserPrincipal().getName());
			if(!Main.isClientAuthorized(sc, configuration)){
				//throw new AuthenticationException("This client is not allowed to use this resource.");
				log.info("Unauthorized access! (SSL)");
			}
			else{
				log.info("Identification is successful! (SSL)");
			}
		}
		
		if(payload.getId() == null){
			log.info("OrchestrationApi:updateEntry throws BadPayloadException.");
			throw new BadPayloadException("Bad payload: id field is missing from the payload.");
		}
		
		restrictionMap.put("id", payload.getId());
		OrchestrationStore storeEntry = dm.get(OrchestrationStore.class, restrictionMap);
		if(storeEntry == null){
			log.info("OrchestrationApi:updateEntry throws DataNotFoundException.");
			throw new DataNotFoundException("Store entry specified by the id(" 
					+ payload.getId() +") was not found in the database.");
		}
		else if(storeEntry.getProviderCloud() != null && payload.getIsActive()){
			log.info("OrchestrationApi:toggleIsActive throws BadPayloadException.");
			throw new BadPayloadException("Only intra-cloud store entries can be active.");
		}
		else{
			storeEntry.setPriority(payload.getPriority());
			storeEntry.setIsActive(payload.getIsActive());
			storeEntry.setName(payload.getName());
			storeEntry.setLastUpdated(new Date());
			storeEntry.setOrchestrationRule(payload.getOrchestrationRule());
			storeEntry = dm.merge(storeEntry);
			
			log.info("updateEntry successfully returns.");
			return Response.status(Status.ACCEPTED).entity(storeEntry).build();
		}
	}

	/**
	 * Deletes the Orchestration Store entry with the name specified by
	 * the path parameter. Returns 200 if the delete is succesful, 204 (no
	 * content) if the entry was not in the database to begin with.
	 * 
	 * @param Integer id
	 */
	@DELETE
	@Path("{id}")
	public Response deleteEntry(@Context SecurityContext sc, @PathParam("id") Integer id) {
		if (sc.isSecure()) {
			log.info("Got a request from a secure channel. Cert: " + sc.getUserPrincipal().getName());
			if(!Main.isClientAuthorized(sc, configuration)){
				//throw new AuthenticationException("This client is not allowed to use this resource.");
				log.info("Unauthorized access! (SSL)");
			}
			else{
				log.info("Identification is successful! (SSL)");
			}
		}
		
		restrictionMap.put("id", id);
		OrchestrationStore entry = dm.get(OrchestrationStore.class, restrictionMap);
		if (entry == null) {
			log.info("deleteEntry had no effect.");
			return Response.noContent().build();
		} else {
			dm.delete(entry);
			log.info("deleteEntry successfully returns.");
			return Response.ok().build();
		}
	}
	
	/**
	 * Deletes the Orchestration Store entries from the database specified by
	 * the consumer. Returns 200 if the delete is succesful, 204 (no content) 
	 * if no matching entries were in the database to begin with.
	 * 
	 * @param OrchestrationStoreQuery query
	 */
	@DELETE
	@Path("systemgroup/{systemGroup}/systemname/{systemName}")
	public Response deleteEntries(@Context SecurityContext sc, @PathParam("systemGroup") String systemGroup, 
			@PathParam("systemName") String systemName){
		if (sc.isSecure()) {
			log.info("Got a request from a secure channel. Cert: " + sc.getUserPrincipal().getName());
			if(!Main.isClientAuthorized(sc, configuration)){
				//throw new AuthenticationException("This client is not allowed to use this resource.");
				log.info("Unauthorized access! (SSL)");
			}
			else{
				log.info("Identification is successful! (SSL)");
			}
		}
		
		List<OrchestrationStore> store = new ArrayList<OrchestrationStore>();
		
		restrictionMap.put("systemGroup", systemGroup);
		restrictionMap.put("systemName", systemName);
		ArrowheadSystem consumer = dm.get(ArrowheadSystem.class, restrictionMap);
		
		restrictionMap.clear();
		restrictionMap.put("consumer", consumer);
		store = dm.getAll(OrchestrationStore.class, restrictionMap);
		if(store.isEmpty()){
			log.info("deleteEntries had no effect.");
			return Response.noContent().build();
		}
		else{
			for(OrchestrationStore entry: store){
				dm.delete(entry);
			}
			
			log.info("deleteEntries successfully returns.");
			return Response.ok().build();
		}
	}

	
}
