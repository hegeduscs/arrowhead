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

	//TODO maybe this has to have the option to be https once we have authentication
	private static final String baseURI = "http://";
	private static DatabaseManager dm = DatabaseManager.getInstance();
	private static HashMap<String, Object> restrictionMap = new HashMap<String, Object>();
	
	private SysConfig(){
	}
	
	/*
	 * Some level of flexibility in the URI creation, in order to avoid
	 * implementation mistakes.
	 */
	public static String getURI(CoreSystem coreSystem) {
		UriBuilder ub = null;
		if (coreSystem.getIPAddress().startsWith("http://")) {
			if (coreSystem.getPort() != null) {
				ub = UriBuilder.fromPath(coreSystem.getIPAddress() + ":" + coreSystem.getPort());
			} else {
				ub = UriBuilder.fromPath(coreSystem.getIPAddress());
			}
		} else {
			if (coreSystem.getPort() != null) {
				ub = UriBuilder.fromPath(baseURI).path(coreSystem.getIPAddress() + ":" + coreSystem.getPort());
			} else {
				ub = UriBuilder.fromPath(baseURI).path(coreSystem.getIPAddress());
			}
		}
		ub.path(coreSystem.getServiceURI());

		return ub.toString();
	}

	public static String getURI(NeighborCloud neighborCloud) {
		UriBuilder ub = null;
		if (neighborCloud.getCloud().getAddress().startsWith("http://")) {
			if (neighborCloud.getCloud().getPort() != null) {
				ub = UriBuilder.fromPath(neighborCloud.getCloud().getAddress() 
						+ ":" + neighborCloud.getCloud().getPort());
			} else {
				ub = UriBuilder.fromPath(neighborCloud.getCloud().getAddress());
			}
		} else {
			if (neighborCloud.getCloud().getPort() != null) {
				ub = UriBuilder.fromPath(baseURI).path(neighborCloud.getCloud().getAddress() 
						+ ":" + neighborCloud.getCloud().getPort());
			} else {
				ub = UriBuilder.fromPath(baseURI).path(neighborCloud.getCloud().getAddress());
			}
		}
		ub.path(neighborCloud.getCloud().getGatekeeperServiceURI());

		return ub.toString();
	}

	public static String getOrchestratorURI() {
		restrictionMap.clear();
		restrictionMap.put("systemName", "orchestration");
		CoreSystem orchestration = dm.get(CoreSystem.class, restrictionMap);
		return getURI(orchestration);
	}

	public static String getServiceRegistryURI() {
		restrictionMap.clear();
		restrictionMap.put("systemName", "serviceregistry");
		CoreSystem serviceRegistry = dm.get(CoreSystem.class, restrictionMap);
		return getURI(serviceRegistry);
	}

	public static String getAuthorizationURI() {
		restrictionMap.clear();
		restrictionMap.put("systemName", "authorization");
		CoreSystem authorization = dm.get(CoreSystem.class, restrictionMap);
		return getURI(authorization);
	}

	public static String getGatekeeperURI() {
		restrictionMap.clear();
		restrictionMap.put("systemName", "gatekeeper");
		CoreSystem gatekeeper = dm.get(CoreSystem.class, restrictionMap);
		return getURI(gatekeeper);
	}

	public static String getQoSURI() {
		restrictionMap.clear();
		restrictionMap.put("systemName", "qos");
		CoreSystem QoS = dm.get(CoreSystem.class, restrictionMap);
		return getURI(QoS);
	}
	
	public static String getApiURI(){
		restrictionMap.clear();
		restrictionMap.put("systemName", "api");
		CoreSystem api = dm.get(CoreSystem.class, restrictionMap);
		return getURI(api);
	}

	public static List<String> getCloudURIs() {
		List<NeighborCloud> cloudList = new ArrayList<NeighborCloud>();
		cloudList.addAll(dm.getAll(NeighborCloud.class, restrictionMap));

		List<String> URIList = new ArrayList<String>();
		for (NeighborCloud cloud : cloudList) {
			URIList.add(getURI(cloud));
		}

		return URIList;
	}

	public static ArrowheadCloud getOwnCloud() {
		List<OwnCloud> cloudList = new ArrayList<OwnCloud>();
		cloudList = dm.getAll(OwnCloud.class, restrictionMap);
		if (cloudList.isEmpty()) {
			throw new DataNotFoundException("No 'Own Cloud' entry in the configuration database."
					+ "Please make sure to enter one in the 'own_cloud' table."
					+ "This information is needed for the Gatekeeper System.");
		}
		OwnCloud retrievedCloud = cloudList.get(0);

		ArrowheadCloud ownCloud = new ArrowheadCloud();
		ownCloud.setOperator(retrievedCloud.getOperator());
		ownCloud.setCloudName(retrievedCloud.getCloudName());
		ownCloud.setAddress(retrievedCloud.getAddress());
		ownCloud.setPort(retrievedCloud.getPort());
		ownCloud.setGatekeeperServiceURI(retrievedCloud.getGatekeeperServiceURI());
		ownCloud.setAuthenticationInfo(retrievedCloud.getAuthenticationInfo());

		return ownCloud;
	}
	

}