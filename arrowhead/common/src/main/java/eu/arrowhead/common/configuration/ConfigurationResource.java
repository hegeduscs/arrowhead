package eu.arrowhead.common.configuration;

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

/**
 * @author umlaufz
 * 
 * This class handles the requests targeted at core/configuration/*.
 * This resource can be used to perform CRUD operations (via REST) on Core Systems 
 * and Neighbor Clouds objects in the configuration database.
 */
@Path("configuration")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConfigurationResource {
	
	SysConfig databaseManager = SysConfig.getInstance();
	
	@GET
	@Path("/coresystems")
	public List<CoreSystem> getAllCoreSystems(){
		return databaseManager.getAll(CoreSystem.class);
	}
	
	@GET
	@Path("/neighborhood")
	public List<NeighborCloud> getAllNeighborClouds(){
		return databaseManager.getAll(NeighborCloud.class);
	}
	
	@GET
	@Path("/owncloud")
	public List<OwnCloud> getAllOwnClouds(){
		return databaseManager.getAll(OwnCloud.class);
	}
	
	@GET
	@Path("/coresystems/{systemName}")
	public Response getSystem(@PathParam("systemName") String systemName){
		CoreSystem coreSystem = databaseManager.getSystem(systemName);
	
		return Response.status(Status.OK).entity(coreSystem).build();
	}
	
	@GET
	@Path("/neighborhood/operator/{operatorName}/cloud/{cloudName}")
	public Response getCloud(@PathParam("operatorName") String operatorName, 
			@PathParam("cloudName") String cloudName){
		NeighborCloud neighborCloud = databaseManager.getCloud(operatorName, cloudName);
	
		return Response.status(Status.OK).entity(neighborCloud).build();
	}
	
	@POST
	@Path("/coresystems/{systemName}")
	public Response saveSystem(@PathParam("systemName") String systemName, NewConfigEntry entry){
		CoreSystem coreSystem = new CoreSystem();
		coreSystem.setSystemName(systemName);
		coreSystem.setIPAddress(entry.getIPAddress());
		coreSystem.setPort(entry.getPort());
		coreSystem.setAuthenticationInfo(entry.getAuthenticationInfo());
		coreSystem.setServiceURI(entry.getServiceURI());
		
		coreSystem = databaseManager.save(coreSystem);
		return Response.status(Status.CREATED).entity(coreSystem).build();
	}
	
	@POST
	@Path("/neighborhood/operator/{operatorName}/cloud/{cloudName}")
	public Response saveCloud(@PathParam("operatorName") String operatorName, 
			@PathParam("cloudName") String cloudName, NewConfigEntry entry){
		NeighborCloud neighborCloud = new NeighborCloud();
		neighborCloud.setOperator(operatorName);
		neighborCloud.setCloudName(cloudName);
		neighborCloud.setIPAddress(entry.getIPAddress());
		neighborCloud.setPort(entry.getPort());
		neighborCloud.setAuthenticationInfo(entry.getAuthenticationInfo());
		neighborCloud.setServiceURI(entry.getServiceURI());
		
		neighborCloud = databaseManager.save(neighborCloud);
		return Response.status(Status.CREATED).entity(neighborCloud).build();
	}
	
	@PUT
	@Path("/coresystems/{systemName}")
	public Response updateSystem(@PathParam("systemName") String systemName, NewConfigEntry entry){
		CoreSystem coreSystem = databaseManager.getSystem(systemName);
		databaseManager.delete(coreSystem);
		coreSystem.setIPAddress(entry.getIPAddress());
		coreSystem.setPort(entry.getPort());
		coreSystem.setAuthenticationInfo(entry.getAuthenticationInfo());
		coreSystem.setServiceURI(entry.getServiceURI());
		
		coreSystem = databaseManager.save(coreSystem);
		return Response.status(Status.CREATED).entity(coreSystem).build();
	}
	
	@PUT
	@Path("/neighborhood/operator/{operatorName}/cloud/{cloudName}")
	public Response updateCloud(@PathParam("operatorName") String operatorName, 
			@PathParam("cloudName") String cloudName, NewConfigEntry entry){
		NeighborCloud neighborCloud = databaseManager.getCloud(operatorName, cloudName);
		databaseManager.delete(neighborCloud);
		neighborCloud.setIPAddress(entry.getIPAddress());
		neighborCloud.setPort(entry.getPort());
		neighborCloud.setAuthenticationInfo(entry.getAuthenticationInfo());
		neighborCloud.setServiceURI(entry.getServiceURI());
		
		neighborCloud = databaseManager.save(neighborCloud);
		return Response.status(Status.CREATED).entity(neighborCloud).build();
	}
	
	@DELETE
	@Path("/coresystems/{systemName}")
	public Response deleteSystem(@PathParam("systemName") String systemName){
		CoreSystem retrievedSystem = databaseManager.getSystem(systemName);
		databaseManager.delete(retrievedSystem);
		return Response.noContent().build();
	}
	
	@DELETE
	@Path("/neighborhood/operator/{operatorName}/cloud/{cloudName}")
	public Response deleteCloud(@PathParam("operatorName") String operatorName, 
			@PathParam("cloudName") String cloudName){
		NeighborCloud retrievedCloud = databaseManager.getCloud(operatorName, cloudName);
		databaseManager.delete(retrievedCloud);
		return Response.noContent().build();
	}

	
}
