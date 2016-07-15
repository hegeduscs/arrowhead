package eu.arrowhead.common.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import eu.arrowhead.common.database.CoreSystem;
import eu.arrowhead.common.database.NeighborCloud;
import eu.arrowhead.common.database.OwnCloud;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.model.ArrowheadCloud;

/**
 * @author umlaufz 
 * 
 * This utility class provides configuration informations to the core systems.
 */
public final class SysConfig {

	private static final String baseURI = "http://";
	private static DatabaseManager dm = DatabaseManager.getInstance();
	private static HashMap<String, Object> restrictionMap = new HashMap<String, Object>();
	
	private SysConfig(){
	}
	
	//TODO felülvizsgálni, hogy lehet-e hatékonyabb, de ekvivalens megoldás ehhez a fgvhez
	/*
	 * Some level of flexibility in the URI creation, in order to avoid
	 * implementation mistakes.
	 */
	public static String getURI(String address, String port, String serviceURI) {
		if(address == null || serviceURI == null){
			throw new NullPointerException("Address and serviceURI can not be null.");
		}
		
		UriBuilder ub = null;
		if (address.startsWith(baseURI)) {
			if (port != null) {
				ub = UriBuilder.fromPath(address + ":" + port);
			} else {
				ub = UriBuilder.fromPath(address);
			}
		} else {
			if (port != null) {
				ub = UriBuilder.fromPath(baseURI).path(address + ":" + port);
			} else {
				ub = UriBuilder.fromPath(baseURI).path(address);
			}
		}
		ub.path(serviceURI);

		return ub.toString();
	}

	public static String getOrchestratorURI() {
		restrictionMap.clear();
		restrictionMap.put("systemName", "orchestrator");
		CoreSystem orchestration = dm.get(CoreSystem.class, restrictionMap);
		if(orchestration == null){
			throw new DataNotFoundException("Orchestration Core System not found in the database!");
		}
		return getURI(orchestration.getAddress(), orchestration.getPort(), orchestration.getServiceURI());
	}

	public static String getServiceRegistryURI() {
		restrictionMap.clear();
		restrictionMap.put("systemName", "serviceregistry");
		CoreSystem serviceRegistry = dm.get(CoreSystem.class, restrictionMap);
		if(serviceRegistry == null){
			throw new DataNotFoundException("Service Registry Core System not found in the database!");
		}
		return getURI(serviceRegistry.getAddress(), serviceRegistry.getPort(), serviceRegistry.getServiceURI());
	}

	public static String getAuthorizationURI() {
		restrictionMap.clear();
		restrictionMap.put("systemName", "authorization");
		CoreSystem authorization = dm.get(CoreSystem.class, restrictionMap);
		if(authorization == null){
			throw new DataNotFoundException("Authoriaztion Core System not found in the database!");
		}
		return getURI(authorization.getAddress(), authorization.getPort(), authorization.getServiceURI());
	}

	public static String getGatekeeperURI() {
		restrictionMap.clear();
		restrictionMap.put("systemName", "gatekeeper");
		CoreSystem gatekeeper = dm.get(CoreSystem.class, restrictionMap);
		if(gatekeeper == null){
			throw new DataNotFoundException("Gatekeeper Core System not found in the database!");
		}
		return getURI(gatekeeper.getAddress(), gatekeeper.getPort(), gatekeeper.getServiceURI());
	}

	public static String getQoSURI() {
		restrictionMap.clear();
		restrictionMap.put("systemName", "qos");
		CoreSystem QoS = dm.get(CoreSystem.class, restrictionMap);
		if(QoS == null){
			throw new DataNotFoundException("QoS Core System not found in the database!");
		}
		return getURI(QoS.getAddress(), QoS.getPort(), QoS.getServiceURI());
	}
	
	public static String getApiURI(){
		restrictionMap.clear();
		restrictionMap.put("systemName", "api");
		CoreSystem api = dm.get(CoreSystem.class, restrictionMap);
		if(api == null){
			throw new DataNotFoundException("API Core System not found in the database!");
		}
		return getURI(api.getAddress(), api.getPort(), api.getServiceURI());
	}

	public static List<String> getNeighborCloudURIs() {
		restrictionMap.clear();
		List<NeighborCloud> cloudList = new ArrayList<NeighborCloud>();
		cloudList.addAll(dm.getAll(NeighborCloud.class, restrictionMap));

		List<String> URIList = new ArrayList<String>();
		for (NeighborCloud cloud : cloudList) {
			URIList.add(getURI(cloud.getCloud().getAddress(),
					cloud.getCloud().getPort(), cloud.getCloud().getGatekeeperServiceURI()));
		}

		return URIList;
	}

	public static ArrowheadCloud getOwnCloud() {
		restrictionMap.clear();
		List<OwnCloud> cloudList = new ArrayList<OwnCloud>();
		cloudList = dm.getAll(OwnCloud.class, restrictionMap);
		if (cloudList.isEmpty()) {
			throw new DataNotFoundException("No 'Own Cloud' entry in the configuration database."
					+ "Please make sure to enter one in the 'own_cloud' table."
					+ "This information is needed for the Gatekeeper System.");
		}

		ArrowheadCloud ownCloud = new ArrowheadCloud(cloudList.get(0));
		return ownCloud;
	}
	
	public static CoreSystem getCoreSystem(String systemName){
		restrictionMap.clear();
		restrictionMap.put("systemName", systemName);
		CoreSystem coreSystem = dm.get(CoreSystem.class, restrictionMap);
		if(coreSystem == null){
			throw new DataNotFoundException("Requested Core System "
					+ "(" + systemName + ") not found in the database!");
		}
		
		return coreSystem;
	}
	

}