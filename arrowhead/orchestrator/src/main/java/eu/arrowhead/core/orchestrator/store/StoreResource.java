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

import org.apache.log4j.Logger;

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
	private static Logger log = Logger.getLogger(StoreResource.class.getName());
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getIt() {
		return "Got it";
	}
	
	/**
	 * Returns all the entries of the Orchestration Store where the priority (int) field
	 * has a value of bigger than 0.
	 * 
	 * @return OrchestrationStoreQueryResponse
	 * @throws DataNotFoundException
	 */
	@GET
	@Path("/all")
	public OrchestrationStoreQueryResponse getAllStoreEntries(){
		log.info("Querying the Orchestration Store for all entries.");
		List<OrchestrationStore> store = new ArrayList<OrchestrationStore>();
		store = storeService.getAllStoreEntries();
		
		Collections.sort(store);
		for(OrchestrationStore entry : store){
			if(entry.getPriority() == 0)
				store.remove(entry);
			else
				break;
		}
		
		if(store.isEmpty()){
			log.info("The Orchestration Store is empty.");
			throw new DataNotFoundException("The Orchestration Store is empty.");
		}
		
		log.info("Returned Orchestration Store size: " + store.size());
		return new OrchestrationStoreQueryResponse(store);
	}
	
	/**
	 * Returns the Orchestration Store entries from the database specified by
	 * the consumer (and the service).
	 * 
	 * @param {OrchestrationStoreQuery} - query
	 * @return OrchestrationStoreQueryResponse
	 * @throws DataNotFoundException, BadPayloadException
	 */
	@PUT
	public OrchestrationStoreQueryResponse getStoreEntries(OrchestrationStoreQuery query){
		List<OrchestrationStore> entryList = new ArrayList<OrchestrationStore>();
		
		/*
		 * If the payload does not have an identifiable requesterSystem
		 * we throw a BadPayloadException.
		 */
		if(!query.isPayloadUsable()){
			log.info("BadPayloadException at the getStoreEntries method.");
			throw new BadPayloadException("Bad payload: mandatory field(s) of requesterSystem "
					+ "is/are missing. (systemGroup, systemName)");
		}
		
		/*
		 * If the onlyActive boolean is set to true, we return all the active
		 * entries belonging to the requesterSystem.
		 */
		else if(query.isOnlyActive()){
			log.info("Querying the Orchestration Store for active entries of the consumer: " 
					+ query.getRequesterSystem());
			List<OrchestrationStore> retrievedList = 
					storeService.getActiveStoreEntries(query.getRequesterSystem());
			if(retrievedList != null && !retrievedList.isEmpty()){
				log.info("Returning the active entry list with a size of " + retrievedList.size());
				Collections.sort(retrievedList);
				return new OrchestrationStoreQueryResponse(retrievedList);
			}
			else{
				log.info("No active Orchestration Store entries were found"
						+ "for this consumer: " + query.getRequesterSystem().getSystemName());
				throw new DataNotFoundException("No active Orchestration Store entries were found"
						+ "for this consumer: " + query.getRequesterSystem().getSystemName()); 
			}
		}
		
		/*
		 * If the payload does not have a requestedService, but the onlyActive boolean is false,
		 * we return all the Orchestration Store entries belonging to the requesterSystem.
		 */
		else if(query.getRequestedService() == null){
			log.info("Querying the Orchestration Store for entries of the consumer: "
					+ query.getRequesterSystem());
			List<OrchestrationStore> retrievedList = 
					storeService.getStoreEntries(query.getRequesterSystem());
			if(retrievedList != null && !retrievedList.isEmpty())
				entryList.addAll(retrievedList);
			else{
				log.info("No Orchestration Store entries were found"
						+ "for this consumer: " + query.getRequesterSystem().getSystemName());
				throw new DataNotFoundException("No Orchestration Store entries were found"
						+ "for this consumer: " + query.getRequesterSystem().getSystemName());
			}
		}
		
		/*
		 * If the payload does have a requestedService, we return all the Orchestration Store
		 * entries specified by the requesterSystem and requestedService.
		 */
		else{
			log.info("Querying the Orchestration Store for entries of the consumer/service pair.");
			List<OrchestrationStore> retrievedList = 
					storeService.getStoreEntries(query.getRequesterSystem(), query.getRequestedService());
			if(retrievedList != null && !retrievedList.isEmpty())
				entryList.addAll(retrievedList);
			else{
				log.info("No Orchestration Store entries were found for this consumer/service pair.");
				throw new DataNotFoundException("No Orchestration Store entries were found"
						+ "for this consumer/service pair: " 
						+ query.getRequesterSystem().getSystemName() + "/"
						+ query.getRequestedService().getServiceDefinition());
			}
		}
		
		Collections.sort(entryList);
		for(OrchestrationStore entry : entryList){
			if(entry.getPriority() == 0)
				entryList.remove(entry);
			else
				break;
		}
		log.info("Returning the Orchestration Store entry list with a size of " + entryList.size());
		
		return new OrchestrationStoreQueryResponse(entryList);
	}
	
	
}
