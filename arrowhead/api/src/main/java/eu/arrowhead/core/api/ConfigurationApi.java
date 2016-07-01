package eu.arrowhead.core.api;

import java.util.ArrayList;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import eu.arrowhead.common.configuration.DatabaseManager;
import eu.arrowhead.common.database.CoreSystem;
import eu.arrowhead.common.database.NeighborCloud;
import eu.arrowhead.common.database.OwnCloud;

@Path("configuration")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConfigurationApi {
	
	DatabaseManager dm = DatabaseManager.getInstance();
	HashMap<String, Object> restrictionMap = new HashMap<String, Object>();
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getIt() {
		return "Got it";
	}
	
	/**
	 * Returns all the Core Systems from the database.
	 * 
	 * @return List<CoreSystem>
	 */
	@GET
	@Path("/coresystems")
	public List<CoreSystem> getAllCoreSystems() {
		return dm.getAll(CoreSystem.class, restrictionMap);
	}

	/**
	 * Returns all the Neighbor Clouds from the database.
	 * 
	 * @return List<NeighborCloud>
	 */
	@GET
	@Path("/neighborhood")
	public List<NeighborCloud> getAllNeighborClouds() {
		return dm.getAll(NeighborCloud.class, restrictionMap);
	}

	/**
	 * Returns all the Own Clouds from the database.
	 * 
	 * @return List<OwnCloud>
	 */
	@GET
	@Path("/owncloud")
	public List<OwnCloud> getAllOwnClouds() {
		return dm.getAll(OwnCloud.class, restrictionMap);
	}

	/**
	 * Returns a Core System from the database specified by the system name.
	 * 
	 * @param {String}
	 *            systemName
	 * @return CoreSystem
	 */
	@GET
	@Path("/coresystems/{systemName}")
	public Response getCoreSystem(@PathParam("systemName") String systemName) {
		restrictionMap.put("systemName", systemName);
		CoreSystem coreSystem = dm.get(CoreSystem.class, restrictionMap);

		return Response.status(Status.OK).entity(coreSystem).build();
	}

	/**
	 * Returns a Neighbor Cloud from the database specified by the operator and
	 * cloud name.
	 * 
	 * @param {String}
	 *            operator
	 * @param {String}
	 *            cloudName
	 * @return NeighborCloud
	 */
	@GET
	@Path("/neighborhood/operator/{operator}/cloud/{cloudName}")
	public Response getNeighborCloud(@PathParam("operator") String operator, @PathParam("cloudName") String cloudName) {
		restrictionMap.put("operator", operator);
		restrictionMap.put("cloudName", cloudName);
		NeighborCloud neighborCloud = dm.get(NeighborCloud.class, restrictionMap);

		return Response.status(Status.OK).entity(neighborCloud).build();
	}

	/**
	 * Adds a list of CoreSystems to the database. Elements which would cause
	 * DuplicateEntryException are being skipped. The returned list only
	 * contains the elements which was saved in the process.
	 *
	 * @param {List<CoreSystem>}
	 *            coreSystemList
	 * @return List<CoreSystem>
	 */
	@POST
	@Path("/coresystems")
	public List<CoreSystem> addCoreSystems(List<CoreSystem> coreSystemList) {
		List<CoreSystem> savedCoreSystems = new ArrayList<CoreSystem>();
		for (CoreSystem cs : coreSystemList) {
			restrictionMap.clear();
			restrictionMap.put("systemName", cs.getSystemName());
			CoreSystem retrievedCoreSystem = dm.get(CoreSystem.class, restrictionMap);
			if (retrievedCoreSystem == null) {
				dm.save(cs);
				savedCoreSystems.add(cs);
			}
		}

		return savedCoreSystems;
	}

	/**
	 * Adds a list of NeighborClouds to the database. Elements which would cause
	 * DuplicateEntryException are being skipped. The returned list only
	 * contains the elements which was saved in the process.
	 *
	 * @param {List<NeighborCloud>}
	 *            coreSystemList
	 * @return List<NeighborCloud>
	 */
	@POST
	@Path("/neighborhood")
	public List<NeighborCloud> addNeighborClouds(List<NeighborCloud> neighborCloudList) {
		List<NeighborCloud> savedNeighborClouds = new ArrayList<NeighborCloud>();
		for (NeighborCloud nc : neighborCloudList) {
			restrictionMap.clear();
			restrictionMap.put("operator", nc.getOperator());
			restrictionMap.put("cloudName", nc.getCloudName());
			NeighborCloud retrievedNeighborCloud = dm.get(NeighborCloud.class, restrictionMap);
			if (retrievedNeighborCloud == null) {
				dm.save(nc);
				savedNeighborClouds.add(nc);
			}
		}

		return savedNeighborClouds;
	}

	/**
	 * Updates an existing CoreSystem in the database. Returns 204 (no content)
	 * if the specified CoreSystem was not in the database.
	 * 
	 * @param {CoreSystem}
	 *            cs
	 */
	@PUT
	@Path("/coresystems")
	public Response updateCoreSystem(CoreSystem cs) {
		restrictionMap.put("systemName", cs.getSystemName());
		CoreSystem coreSystem = dm.get(CoreSystem.class, restrictionMap);
		if (coreSystem != null) {
			dm.delete(coreSystem);
			coreSystem.setIPAddress(cs.getIPAddress());
			coreSystem.setPort(cs.getPort());
			coreSystem.setAuthenticationInfo(cs.getAuthenticationInfo());
			coreSystem.setServiceURI(cs.getServiceURI());

			coreSystem = dm.save(coreSystem);
			return Response.status(Status.ACCEPTED).entity(coreSystem).build();
		} else {
			return Response.noContent().build();
		}

	}

	/**
	 * Updates an existing NeighborCloud in the database. Returns 204 (no
	 * content) if the specified NeighborCloud was not in the database.
	 * 
	 * @param {NeighborCloud}
	 *            nc
	 */
	@PUT
	@Path("/neighborhood")
	public Response updateNeighborCloud(NeighborCloud nc) {
		restrictionMap.put("operator", nc.getOperator());
		restrictionMap.put("cloudName", nc.getCloudName());
		NeighborCloud neighborCloud = dm.get(NeighborCloud.class, restrictionMap);
		if (neighborCloud != null) {
			dm.delete(neighborCloud);
			neighborCloud.setIPAddress(nc.getIPAddress());
			neighborCloud.setPort(nc.getPort());
			neighborCloud.setAuthenticationInfo(nc.getAuthenticationInfo());
			neighborCloud.setServiceURI(nc.getServiceURI());

			neighborCloud = dm.save(neighborCloud);
			return Response.status(Status.ACCEPTED).entity(neighborCloud).build();
		} else {
			return Response.noContent().build();
		}

	}

	/**
	 * Deletes the CoreSystem from the database specified by the system name.
	 * Returns 200 if the delete is succesful, 204 (no content) if the system
	 * was not in the database to begin with.
	 * 
	 * @param {String}
	 *            systemName
	 */
	@DELETE
	@Path("/coresystems/{systemName}")
	public Response deleteCoreSystem(@PathParam("systemName") String systemName) {
		restrictionMap.put("systemName", systemName);
		CoreSystem retrievedSystem = dm.get(CoreSystem.class, restrictionMap);
		if (retrievedSystem == null) {
			return Response.noContent().build();
		} else {
			dm.delete(retrievedSystem);
			return Response.ok().build();
		}
	}

	/**
	 * Deletes the NeighborCloud from the database specified by the operator and
	 * cloud name. Returns 200 if the delete is succesful, 204 (no content) if
	 * the system was not in the database to begin with.
	 * 
	 * @param {String}
	 *            operator
	 * @param {String}
	 *            cloudName
	 */
	@DELETE
	@Path("/neighborhood/operator/{operator}/cloud/{cloudName}")
	public Response deleteNeighborCloud(@PathParam("operator") String operator,
			@PathParam("cloudName") String cloudName) {
		restrictionMap.put("operator", operator);
		restrictionMap.put("cloudName", cloudName);
		NeighborCloud retrievedCloud = dm.get(NeighborCloud.class, restrictionMap);
		if (retrievedCloud == null) {
			return Response.noContent().build();
		} else {
			dm.delete(retrievedCloud);
			return Response.ok().build();
		}
	}
	
	
}
