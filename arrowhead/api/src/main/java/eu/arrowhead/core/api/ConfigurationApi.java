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
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import eu.arrowhead.common.configuration.DatabaseManager;
import eu.arrowhead.common.database.CoreSystem;
import eu.arrowhead.common.database.NeighborCloud;
import eu.arrowhead.common.database.OwnCloud;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.model.ArrowheadCloud;

@Path("configuration")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConfigurationApi {
	
	@Context
	Configuration configuration;
	DatabaseManager dm = DatabaseManager.getInstance();
	HashMap<String, Object> restrictionMap = new HashMap<String, Object>();
	private static Logger log = Logger.getLogger(ConfigurationApi.class.getName());
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getIt(@Context SecurityContext sc) {
		if (sc.isSecure()) {
			log.info("Got a request from a secure channel. Cert: " + sc.getUserPrincipal().getName());
			if(!ApiMain.isClientAuthorized(sc, configuration)){
				//throw new AuthenticationException("This client is not allowed to use this resource.");
				log.info("Unauthorized access! (SSL)");
			}
			else{
				log.info("Identification is successful! (SSL)");
			}
		}
		
		return "Got it";
	}
	
	/**
	 * Returns all the Core Systems from the database.
	 * 
	 * @return List<CoreSystem>
	 * @throws DataNotFoundException
	 */
	@GET
	@Path("/coresystems")
	public List<CoreSystem> getAllCoreSystems(@Context SecurityContext sc) {
		if (sc.isSecure()) {
			log.info("Got a request from a secure channel. Cert: " + sc.getUserPrincipal().getName());
			if(!ApiMain.isClientAuthorized(sc, configuration)){
				//throw new AuthenticationException("This client is not allowed to use this resource.");
				log.info("Unauthorized access! (SSL)");
			}
			else{
				log.info("Identification is successful! (SSL)");
			}
		}
		
		List<CoreSystem> systemList = new ArrayList<CoreSystem>();
		systemList = dm.getAll(CoreSystem.class, restrictionMap);
		if(systemList.isEmpty()){
			log.info("ConfigurationApi:getAllCoreSystems throws DataNotFoundException");
			throw new DataNotFoundException("CoreSystems not found in the database.");
		}
		
		return systemList;
	}

	/**
	 * Returns all the Neighbor Clouds from the database.
	 * 
	 * @return List<NeighborCloud>
	 * @throws DataNotFoundException
	 */
	@GET
	@Path("/neighborhood")
	public List<NeighborCloud> getAllNeighborClouds(@Context SecurityContext sc) {
		if (sc.isSecure()) {
			log.info("Got a request from a secure channel. Cert: " + sc.getUserPrincipal().getName());
			if(!ApiMain.isClientAuthorized(sc, configuration)){
				//throw new AuthenticationException("This client is not allowed to use this resource.");
				log.info("Unauthorized access! (SSL)");
			}
			else{
				log.info("Identification is successful! (SSL)");
			}
		}
		
		List<NeighborCloud> cloudList = new ArrayList<NeighborCloud>();
		cloudList = dm.getAll(NeighborCloud.class, restrictionMap);
		if(cloudList.isEmpty()){
			log.info("ConfigurationApi:getAllNeighborClouds throws DataNotFoundException");
			throw new DataNotFoundException("NeighborClouds not found in the database.");
		}
		
		return cloudList;
	}

	/**
	 * Returns all the Own Clouds from the database.
	 * 
	 * @return List<OwnCloud>
	 * @throws DataNotFoundException
	 */
	@GET
	@Path("/owncloud")
	public List<OwnCloud> getAllOwnClouds(@Context SecurityContext sc) {
		if (sc.isSecure()) {
			log.info("Got a request from a secure channel. Cert: " + sc.getUserPrincipal().getName());
			if(!ApiMain.isClientAuthorized(sc, configuration)){
				//throw new AuthenticationException("This client is not allowed to use this resource.");
				log.info("Unauthorized access! (SSL)");
			}
			else{
				log.info("Identification is successful! (SSL)");
			}
		}
		
		List<OwnCloud> cloudList = new ArrayList<OwnCloud>();
		cloudList = dm.getAll(OwnCloud.class, restrictionMap);
		if(cloudList.isEmpty()){
			log.info("ConfigurationApi:getAllOwnClouds throws DataNotFoundException");
			throw new DataNotFoundException("OwnClouds not found in the database.");
		}
		
		return cloudList;
	}

	/**
	 * Returns a Core System from the database specified by the system name.
	 * 
	 * @param String systemName
	 * @return CoreSystem
	 * @throws DataNotFoundException
	 */
	@GET
	@Path("/coresystems/{systemName}")
	public Response getCoreSystem(@Context SecurityContext sc, @PathParam("systemName") String systemName) {
		if (sc.isSecure()) {
			log.info("Got a request from a secure channel. Cert: " + sc.getUserPrincipal().getName());
			if(!ApiMain.isClientAuthorized(sc, configuration)){
				//throw new AuthenticationException("This client is not allowed to use this resource.");
				log.info("Unauthorized access! (SSL)");
			}
			else{
				log.info("Identification is successful! (SSL)");
			}
		}
		
		restrictionMap.put("systemName", systemName);
		CoreSystem coreSystem = dm.get(CoreSystem.class, restrictionMap);
		if(coreSystem == null){
			log.info("ConfigurationApi:getCoreSystem throws DataNotFoundException");
			throw new DataNotFoundException("Requested CoreSystem not found in the database.");
		}

		return Response.status(Status.OK).entity(coreSystem).build();
	}

	/**
	 * Returns a Neighbor Cloud from the database specified by the operator and
	 * cloud name.
	 * 
	 * @param String operator
	 * @param String cloudName
	 * @return NeighborCloud
	 * @throws DataNotFoundException
	 */
	@GET
	@Path("/neighborhood/operator/{operator}/cloudname/{cloudName}")
	public Response getNeighborCloud(@Context SecurityContext sc, @PathParam("operator") String operator, 
			@PathParam("cloudName") String cloudName) {
		if (sc.isSecure()) {
			log.info("Got a request from a secure channel. Cert: " + sc.getUserPrincipal().getName());
			if(!ApiMain.isClientAuthorized(sc, configuration)){
				//throw new AuthenticationException("This client is not allowed to use this resource.");
				log.info("Unauthorized access! (SSL)");
			}
			else{
				log.info("Identification is successful! (SSL)");
			}
		}
		
		restrictionMap.put("operator", operator);
		restrictionMap.put("cloudName", cloudName);
		ArrowheadCloud cloud = dm.get(ArrowheadCloud.class, restrictionMap);
		if(cloud == null){
			log.info("ConfigurationApi:getNeighborCloud throws DataNotFoundException");
			throw new DataNotFoundException("Requested NeighborCloud not found in the database.");
		}
		
		restrictionMap.clear();
		restrictionMap.put("cloud", cloud);
		NeighborCloud neighborCloud = dm.get(NeighborCloud.class, restrictionMap);
		if(neighborCloud == null){
			log.info("ConfigurationApi:getNeighborCloud throws DataNotFoundException");
			throw new DataNotFoundException("Requested NeighborCloud not found in the database.");
		}

		return Response.status(Status.OK).entity(neighborCloud).build();
	}

	/**
	 * Adds a list of CoreSystems to the database. Elements which would
	 * cause DuplicateEntryException or BadPayloadException 
	 * (caused by missing systemName, address or serviceURI) are being skipped. 
	 * The returned list only contains the elements which was saved in the process.
	 *
	 * @param List<CoreSystem> coreSystemList
	 * @return List<CoreSystem>
	 */
	@POST
	@Path("/coresystems")
	public List<CoreSystem> addCoreSystems(@Context SecurityContext sc, List<CoreSystem> coreSystemList) {
		if (sc.isSecure()) {
			log.info("Got a request from a secure channel. Cert: " + sc.getUserPrincipal().getName());
			if(!ApiMain.isClientAuthorized(sc, configuration)){
				//throw new AuthenticationException("This client is not allowed to use this resource.");
				log.info("Unauthorized access! (SSL)");
			}
			else{
				log.info("Identification is successful! (SSL)");
			}
		}
		
		List<CoreSystem> savedCoreSystems = new ArrayList<CoreSystem>();
		for (CoreSystem cs : coreSystemList) {
			if(cs.isPayloadUsable()){
				restrictionMap.clear();
				restrictionMap.put("systemName", cs.getSystemName());
				CoreSystem retrievedCoreSystem = dm.get(CoreSystem.class, restrictionMap);
				if (retrievedCoreSystem == null) {
					dm.save(cs);
					savedCoreSystems.add(cs);
				}
			}
		}

		return savedCoreSystems;
	}

	/**
	 * Adds a list of NeighborClouds to the database. Elements which would
	 * cause DuplicateEntryException or BadPayloadException 
	 * (caused by missing operator, cloudName, address or serviceURI) are being skipped. 
	 * The returned list only contains the elements which was saved in the process.
	 *
	 * @param List<NeighborCloud> coreSystemList
	 * @return List<NeighborCloud>
	 */
	@POST
	@Path("/neighborhood")
	public List<NeighborCloud> addNeighborClouds(@Context SecurityContext sc, List<NeighborCloud> neighborCloudList) {
		if (sc.isSecure()) {
			log.info("Got a request from a secure channel. Cert: " + sc.getUserPrincipal().getName());
			if(!ApiMain.isClientAuthorized(sc, configuration)){
				//throw new AuthenticationException("This client is not allowed to use this resource.");
				log.info("Unauthorized access! (SSL)");
			}
			else{
				log.info("Identification is successful! (SSL)");
			}
		}
		
		List<NeighborCloud> savedNeighborClouds = new ArrayList<NeighborCloud>();
		for (NeighborCloud nc : neighborCloudList) {
			if(nc.isPayloadUsable()){
				restrictionMap.clear();
				restrictionMap.put("operator", nc.getCloud().getOperator());
				restrictionMap.put("cloudName", nc.getCloud().getCloudName());
				ArrowheadCloud cloud = dm.get(ArrowheadCloud.class, restrictionMap);
				if (cloud == null) {
					dm.save(nc.getCloud());
				}
				
				restrictionMap.clear();
				restrictionMap.put("cloud", cloud);
				NeighborCloud neighborCloud = dm.get(NeighborCloud.class, restrictionMap);
				if(neighborCloud == null){
					dm.merge(nc);
					savedNeighborClouds.add(nc);
				}
			}
		}

		return savedNeighborClouds;
	}
	
	/**
	 * Adds an instance of OwnCloud to the database. Deletes any other row 
	 * in this table, before doing this save. Missing operator, 
	 * cloudName or address causes BadPayloadException!
	 *
	 * @param OwnCloud ownCloud
	 * @return OwnCloud
	 * @throws BadPayloadException
	 */
	@POST
	@Path("/owncloud")
	public OwnCloud addOwnCloud(@Context SecurityContext sc, OwnCloud ownCloud) {
		if (sc.isSecure()) {
			log.info("Got a request from a secure channel. Cert: " + sc.getUserPrincipal().getName());
			if(!ApiMain.isClientAuthorized(sc, configuration)){
				//throw new AuthenticationException("This client is not allowed to use this resource.");
				log.info("Unauthorized access! (SSL)");
			}
			else{
				log.info("Identification is successful! (SSL)");
			}
		}
		
		if(!ownCloud.isPayloadUsable()){
			log.info("ConfigurationApi:addOwnCloud throws BadPayloadException");
			throw new BadPayloadException("Bad payload: missing operator, cloudName "
					+ "or address field! (ConfigurationApi:addOwnCloud)");
		}
		
		List<OwnCloud> ownClouds = new ArrayList<OwnCloud>();
		ownClouds = dm.getAll(OwnCloud.class, restrictionMap);
		if(!ownClouds.isEmpty()){
			for(OwnCloud cloud : ownClouds){
				dm.delete(cloud);
			}
		}
		
		ownCloud = dm.save(ownCloud);
		return ownCloud;
	}

	/**
	 * Updates an existing CoreSystem in the database. Returns 204 (no content)
	 * if the specified CoreSystem was not in the database.
	 * 
	 * @param CoreSystem cs
	 * @throws BadPayloadException
	 */
	@PUT
	@Path("/coresystems")
	public Response updateCoreSystem(@Context SecurityContext sc, CoreSystem cs) {
		if (sc.isSecure()) {
			log.info("Got a request from a secure channel. Cert: " + sc.getUserPrincipal().getName());
			if(!ApiMain.isClientAuthorized(sc, configuration)){
				//throw new AuthenticationException("This client is not allowed to use this resource.");
				log.info("Unauthorized access! (SSL)");
			}
			else{
				log.info("Identification is successful! (SSL)");
			}
		}
		
		if(!cs.isPayloadUsable()){
			log.info("ConfigurationApi:updateCoreSystem throws BadPayloadException");
			throw new BadPayloadException("Bad payload: missing systemName, address or "
					+ "serviceURI in the entry payload.");
		}
		
		restrictionMap.put("systemName", cs.getSystemName());
		CoreSystem coreSystem = dm.get(CoreSystem.class, restrictionMap);
		if (coreSystem != null) {
			coreSystem.setAddress(cs.getAddress());
			coreSystem.setPort(cs.getPort());
			coreSystem.setAuthenticationInfo(cs.getAuthenticationInfo());
			coreSystem.setServiceURI(cs.getServiceURI());

			coreSystem = dm.merge(coreSystem);
			return Response.status(Status.ACCEPTED).entity(coreSystem).build();
		} else {
			return Response.noContent().build();
		}

	}

	/**
	 * Updates an existing NeighborCloud in the database. Returns 204 (no
	 * content) if the specified NeighborCloud was not in the database.
	 * 
	 * @param NeighborCloud nc
	 * @throws BadPayloadException
	 */
	@PUT
	@Path("/neighborhood")
	public Response updateNeighborCloud(@Context SecurityContext sc, NeighborCloud nc) {
		if (sc.isSecure()) {
			log.info("Got a request from a secure channel. Cert: " + sc.getUserPrincipal().getName());
			if(!ApiMain.isClientAuthorized(sc, configuration)){
				//throw new AuthenticationException("This client is not allowed to use this resource.");
				log.info("Unauthorized access! (SSL)");
			}
			else{
				log.info("Identification is successful! (SSL)");
			}
		}
		
		if(!nc.isPayloadUsable()){
			log.info("ConfigurationApi:updateNeighborCloud throws BadPayloadException");
			throw new BadPayloadException("Bad payload: missing/incomplete arrowheadcloud"
					+ "in the entry payload.");
		}
		
		restrictionMap.put("operator", nc.getCloud().getOperator());
		restrictionMap.put("cloudName", nc.getCloud().getCloudName());
		ArrowheadCloud cloud = dm.get(ArrowheadCloud.class, restrictionMap);
		
		restrictionMap.clear();
		restrictionMap.put("cloud", cloud);
		NeighborCloud neighborCloud = dm.get(NeighborCloud.class, restrictionMap);
		if (neighborCloud != null) {
			neighborCloud.getCloud().setAddress(nc.getCloud().getAddress());
			neighborCloud.getCloud().setPort(nc.getCloud().getPort());
			neighborCloud.getCloud().setAuthenticationInfo(nc.getCloud().getAuthenticationInfo());
			neighborCloud.getCloud().setGatekeeperServiceURI(nc.getCloud().getGatekeeperServiceURI());

			neighborCloud = dm.merge(neighborCloud);
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
	 * @param String systemName
	 */
	@DELETE
	@Path("/coresystems/{systemName}")
	public Response deleteCoreSystem(@Context SecurityContext sc, @PathParam("systemName") String systemName) {
		if (sc.isSecure()) {
			log.info("Got a request from a secure channel. Cert: " + sc.getUserPrincipal().getName());
			if(!ApiMain.isClientAuthorized(sc, configuration)){
				//throw new AuthenticationException("This client is not allowed to use this resource.");
				log.info("Unauthorized access! (SSL)");
			}
			else{
				log.info("Identification is successful! (SSL)");
			}
		}
		
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
	 * @param String operator
	 * @param String cloudName
	 */
	@DELETE
	@Path("/neighborhood/operator/{operator}/cloudname/{cloudName}")
	public Response deleteNeighborCloud(@Context SecurityContext sc, @PathParam("operator") String operator,
			@PathParam("cloudName") String cloudName) {
		if (sc.isSecure()) {
			log.info("Got a request from a secure channel. Cert: " + sc.getUserPrincipal().getName());
			if(!ApiMain.isClientAuthorized(sc, configuration)){
				//throw new AuthenticationException("This client is not allowed to use this resource.");
				log.info("Unauthorized access! (SSL)");
			}
			else{
				log.info("Identification is successful! (SSL)");
			}
		}
		
		restrictionMap.put("operator", operator);
		restrictionMap.put("cloudName", cloudName);
		ArrowheadCloud cloud = dm.get(ArrowheadCloud.class, restrictionMap);
		if(cloud == null){
			return Response.noContent().build();
		}
		
		restrictionMap.clear();
		restrictionMap.put("cloud", cloud);
		NeighborCloud neighborCloud = dm.get(NeighborCloud.class, restrictionMap);
		if(neighborCloud == null){
			return Response.noContent().build();
		}
		else {
			dm.delete(neighborCloud);
			return Response.ok().build();
		}
	}
	
	
}
