package eu.arrowhead.core.gatekeeper;

import java.util.HashMap;

import org.apache.log4j.Logger;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.database.CoreSystem;
import eu.arrowhead.core.gatekeeper.GatekeeperService;

public class GatekeeperService {

	//private static Logger log = Logger.getLogger(GatekeeperService.class.getName());

	static public CoreSystem getGetawaySystem() {

		HashMap<String, Object> restrictionMap = new HashMap<>();
		DatabaseManager dm = DatabaseManager.getInstance();
		restrictionMap.put("systemName", "gateway");
		CoreSystem gateway = dm.get(CoreSystem.class, restrictionMap);

		if (gateway == null) {
			throw new RuntimeException("Gateway Core System not found in the database!");
		}
		return gateway;

	}

}
