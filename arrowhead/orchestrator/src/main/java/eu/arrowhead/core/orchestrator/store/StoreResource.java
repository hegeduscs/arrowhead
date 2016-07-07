package eu.arrowhead.core.orchestrator.store;


import java.util.ArrayList;
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
	 * This method queries the Orchestration Store. Based on the payload it can return all 
	 * of the store entries, get a specific entry (consumer/onlyActive or consumer/service 
	 * pairs specify an entry) or all of the entries belonging to a consumer.
	 * 
	 * @param {OrchestrationStoreQuery} - query
	 * @return OrchestrationStoreQueryResponse
	 * @throws DataNotFoundException, BadPayloadException
	 */
	@PUT
	public OrchestrationStoreQueryResponse getConfigurations(OrchestrationStoreQuery query){
		List<OrchestrationStore> entryList = new ArrayList<OrchestrationStore>();
		if(query.isPayloadEmpty()){
			List<OrchestrationStore> store = storeService.getAllStoreEntries();
			if(store != null)
				entryList.addAll(store);
			else{
				throw new DataNotFoundException("Orchestration Store is empty.");
			}
		}
		else if(query.isPayloadUsable()){
			OrchestrationStore entry = storeService.getStoreEntry(query.getRequesterSystem(), 
					query.getRequestedService());
			if(entry != null)
				entryList.add(entry);
			else{
				throw new DataNotFoundException("Requested Store entry was not found in the database."); 
			}
		}
		else if(query.isOnlyActive() && query.getRequesterSystem() != null && 
				query.getRequesterSystem().isValid()){
			OrchestrationStore entry = storeService.getActiveStoreEntry(query.getRequesterSystem());
			if(entry != null)
				entryList.add(entry);
			else{
				throw new DataNotFoundException("Active Store entry for this consumer "
						+ "was not found in the database.");
			}
		}
		else if(query.getRequesterSystem() != null && query.getRequesterSystem().isValid()){
			List<OrchestrationStore> retrievedList = 
					storeService.getStoreEntries(query.getRequesterSystem());
			if(retrievedList != null)
				entryList.addAll(retrievedList);
			else{
				throw new DataNotFoundException("Store entries for this consumer were not found"
						+ "in the database.");
			}
		}
		else{
			throw new BadPayloadException("Bad payload: mandatory field(s) of requesterSystem "
					+ "is/are missing. (systemGroup, systemName)");
		}
		
		return new OrchestrationStoreQueryResponse(entryList);
	}
	
	
}
