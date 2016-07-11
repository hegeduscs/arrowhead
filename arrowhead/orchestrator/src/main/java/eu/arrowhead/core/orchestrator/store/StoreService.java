package eu.arrowhead.core.orchestrator.store;

import java.util.HashMap;
import java.util.List;

import eu.arrowhead.common.configuration.DatabaseManager;
import eu.arrowhead.common.database.OrchestrationStore;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;

public class StoreService {
	
	DatabaseManager dm = DatabaseManager.getInstance();
	HashMap<String, Object> restrictionMap = new HashMap<String, Object>();
	
	/**
	 * This method returns an Orchestration Store entry specified by the consumer system
	 * and the requested service.
	 */
	public OrchestrationStore getStoreEntry(ArrowheadSystem consumer, ArrowheadService service){
		ArrowheadSystem savedConsumer = getConsumerSystem(consumer.getSystemGroup(), 
				consumer.getSystemName());
		ArrowheadService savedService = getRequestedService(service.getServiceGroup(),
				service.getServiceDefinition());
		if(savedConsumer == null || savedService == null)
			return null;
		
		restrictionMap.put("consumer", savedConsumer);
		restrictionMap.put("service", savedService);
		return dm.get(OrchestrationStore.class, restrictionMap);
	}

	/**
	 * This method returns the active Orchestration Store entry for a consumer.
	 */
	public OrchestrationStore getActiveStoreEntry(ArrowheadSystem consumer){
		ArrowheadSystem savedConsumer = new ArrowheadSystem();
		savedConsumer =	getConsumerSystem(consumer.getSystemGroup(), consumer.getSystemName());
		if(savedConsumer == null)
			return null;
			
		restrictionMap.put("consumer", savedConsumer);
		restrictionMap.put("isActive", true);
		return dm.get(OrchestrationStore.class, restrictionMap);
	}
	
	/**
	 * This method returns all the Orchestration Store entries belonging to a consumer.
	 */
	public List<OrchestrationStore> getStoreEntries(ArrowheadSystem consumer){
		ArrowheadSystem savedConsumer = getConsumerSystem(consumer.getSystemGroup(), 
				consumer.getSystemName());
		if(savedConsumer == null)
			return null;
		
		restrictionMap.put("consumer", savedConsumer);
		return dm.getAll(OrchestrationStore.class, restrictionMap);
	}
	
	/**
	 * This method returns all the entries of the Orchestration Store.
	 */
	public List<OrchestrationStore> getAllStoreEntries(){
		return dm.getAll(OrchestrationStore.class, restrictionMap);
	}
	
	/**
	 * This method returns an ArrowheadSystem from the database.
	 */
	public ArrowheadSystem getConsumerSystem(String systemGroup, String systemName){
		HashMap<String, Object> rm = new HashMap<String, Object>();
		rm.put("systemGroup", systemGroup);
		rm.put("systemName", systemName);
		return dm.get(ArrowheadSystem.class, rm);
	}
	
	/**
	 * This method returns an ArrowheadService from the database.
	 */
	public ArrowheadService getRequestedService(String serviceGroup, String serviceDefinition){
		HashMap<String, Object> rm = new HashMap<String, Object>();
		rm.put("serviceGroup", serviceGroup);
		rm.put("serviceDefinition", serviceDefinition);
		return dm.get(ArrowheadService.class, rm);
	}
	
}
