package eu.arrowhead.core.orchestrator.store;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import eu.arrowhead.common.database.OrchestrationStore;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.model.messages.OrchestrationStoreQuery;
import eu.arrowhead.common.model.messages.OrchestrationStoreQueryResponse;

/**
 * @author umlaufz
 *
 */
@Path("store")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StoreResource {
	
	private StoreService storeService = new StoreService();
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getIt() {
		return "Got it";
	}
	
	/**
	 * Returns all the entries of the Orchestration Store.
	 * 
	 * @return OrchestrationStoreQueryResponse
	 * @throws DataNotFoundException
	 */
	@GET
	@Path("/all")
	public OrchestrationStoreQueryResponse getAllConfigurations(){
		List<OrchestrationStore> store = new ArrayList<OrchestrationStore>();
		store = storeService.getAllStoreEntries();
		if(store.isEmpty()){
			throw new DataNotFoundException("The Orchestration Store is empty.");
		}
		
		Collections.sort(store);
		return new OrchestrationStoreQueryResponse(store);
	}
	
	/**
	 * Returns the Orchestration Store entries from the database specified by
	 * the consumer and/or the service.
	 * 
	 * @param {OrchestrationStoreQuery} - query
	 * @return OrchestrationStoreQueryResponse
	 * @throws DataNotFoundException, BadPayloadException
	 */
	@PUT
	public OrchestrationStoreQueryResponse getConfigurations(OrchestrationStoreQuery query){
		List<OrchestrationStore> entryList = new ArrayList<OrchestrationStore>();
		
		/*
		 * If the payload does not have a identifiable requesterSystem
		 * we throw a BadPayloadException.
		 */
		if(query.getRequesterSystem() == null || !query.getRequesterSystem().isValid()){
			throw new BadPayloadException("Bad payload: mandatory field(s) of requesterSystem "
					+ "is/are missing. (systemGroup, systemName)");
		}
		
		/*
		 * If the payload has both the requesterSystem and the requestedService,
		 * we return the matching Orchestration Store entry.
		 */
		else if(query.isPayloadComplete()){
			OrchestrationStore entry = storeService.getStoreEntry(query.getRequesterSystem(), 
					query.getRequestedService());
			if(entry != null)
				entryList.add(entry);
			else{
				throw new DataNotFoundException("Requested Store entry was not found in the database."); 
			}
		}
		
		/*
		 * If the payload does not have a requestedService, but the onlyActive boolean is true,
		 * we return the active store entry belonging to the requesterSystem.
		 */
		else if(query.isOnlyActive()){
			OrchestrationStore entry = storeService.getActiveStoreEntry(query.getRequesterSystem());
			if(entry != null)
				entryList.add(entry);
			else{
				throw new DataNotFoundException("Active Store entry for this consumer "
						+ "was not found in the database.");
			}
		}
		
		/*
		 * If the onlyActive boolean is false, we return all the store entries 
		 * belonging to the requesterSystem.
		 */
		else{
			List<OrchestrationStore> retrievedList = 
					storeService.getStoreEntries(query.getRequesterSystem());
			if(retrievedList != null)
				entryList.addAll(retrievedList);
			else{
				throw new DataNotFoundException("Store entries for this consumer were not found"
						+ "in the database.");
			}
		}
		
		Collections.sort(entryList);
		return new OrchestrationStoreQueryResponse(entryList);
	}
	
	
}
