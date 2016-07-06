package eu.arrowhead.core.orchestrator.store;

import java.util.HashMap;

import eu.arrowhead.common.configuration.DatabaseManager;
import eu.arrowhead.common.database.OrchestrationStore;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;

public class StoreService {
	
	DatabaseManager dm = DatabaseManager.getInstance();
	HashMap<String, Object> restrictionMap = new HashMap<String, Object>();
	
	public OrchestrationStore getStoreEntry(ArrowheadSystem consumer, ArrowheadService service){
		
		return null;
	}
}
