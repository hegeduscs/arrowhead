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
 * @author umlaufz This class serves as a Database Acces Object for the
 *         database, and provides URI information for the core systems.
 */
public class SysConfig {

	private static final String baseURI = "http://";
	public DatabaseManager dm = DatabaseManager.getInstance();
	HashMap<String, Object> restrictionMap = new HashMap<String, Object>();
	
	/*
	 * Some level of flexibility in the URI creation, in order to avoid
	 * implementation mistakes.
	 */
	public String getURI(CoreSystem coreSystem) {
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

	public String getURI(NeighborCloud neighborCloud) {
		UriBuilder ub = null;
		if (neighborCloud.getIPAddress().startsWith("http://")) {
			if (neighborCloud.getPort() != null) {
				ub = UriBuilder.fromPath(neighborCloud.getIPAddress() + ":" + neighborCloud.getPort());
			} else {
				ub = UriBuilder.fromPath(neighborCloud.getIPAddress());
			}
		} else {
			if (neighborCloud.getPort() != null) {
				ub = UriBuilder.fromPath(baseURI).path(neighborCloud.getIPAddress() + ":" + neighborCloud.getPort());
			} else {
				ub = UriBuilder.fromPath(baseURI).path(neighborCloud.getIPAddress());
			}
		}
		ub.path(neighborCloud.getServiceURI());

		return ub.toString();
	}

	public String getOrchestratorURI() {
		restrictionMap.clear();
		restrictionMap.put("systemName", "orchestration");
		CoreSystem orchestration = dm.get(CoreSystem.class, restrictionMap);
		return getURI(orchestration);
	}

	public String getServiceRegistryURI() {
		restrictionMap.clear();
		restrictionMap.put("systemName", "serviceregistry");
		CoreSystem serviceRegistry = dm.get(CoreSystem.class, restrictionMap);
		return getURI(serviceRegistry);
	}

	public String getAuthorizationURI() {
		restrictionMap.clear();
		restrictionMap.put("systemName", "authorization");
		CoreSystem authorization = dm.get(CoreSystem.class, restrictionMap);
		return getURI(authorization);
	}

	public String getGatekeeperURI() {
		restrictionMap.clear();
		restrictionMap.put("systemName", "gatekeeper");
		CoreSystem gatekeeper = dm.get(CoreSystem.class, restrictionMap);
		return getURI(gatekeeper);
	}

	public String getQoSURI() {
		restrictionMap.clear();
		restrictionMap.put("systemName", "qos");
		CoreSystem QoS = dm.get(CoreSystem.class, restrictionMap);
		return getURI(QoS);
	}

	public List<String> getCloudURIs() {
		List<NeighborCloud> cloudList = new ArrayList<NeighborCloud>();
		cloudList.addAll(dm.getAll(NeighborCloud.class, restrictionMap));

		List<String> URIList = new ArrayList<String>();
		for (NeighborCloud cloud : cloudList) {
			URIList.add(getURI(cloud));
		}

		return URIList;
	}

	public ArrowheadCloud getOwnCloud() {
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
		ownCloud.setAddress(retrievedCloud.getIPAddress());
		ownCloud.setPort(retrievedCloud.getPort());
		ownCloud.setGatekeeperServiceURI(retrievedCloud.getServiceURI());
		ownCloud.setAuthenticationInfo(retrievedCloud.getAuthenticationInfo());

		return ownCloud;
	}

}