package eu.arrowhead.core.api;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import eu.arrowhead.common.configuration.SysConfig;
import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;

/**
 * @author uzoltan
 *
 * REST resource for arrowhead management tools. 
 * TODO: adding more functions 
 */
@Path("api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApiResource {

	SysConfig dm = SysConfig.getInstance();
			
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {  	
      return "Got it";
    }
    
	/**
	 * Returns the list of ArrowheadServices from the database.
	 *
	 * @return List<ArrowheadService>
	 */
    @GET
    @Path("/services")
    public List<ArrowheadService> getAllServices(){
    	List<ArrowheadService> serviceList = new ArrayList<ArrowheadService>();
    	serviceList = dm.getAll(ArrowheadService.class);
    	
    	return serviceList;
    }
    
	/**
	 * Adds a list of ArrowheadServices to the database. 
	 * Elements which would cause DuplicateEntryException are being skipped.
	 * The returned list only contains the elements which was saved in the process.
	 *
	 * @param {List<ArrowheadService>} serviceList 
	 * @return List<ArrowheadService>
	 */
    @POST
    @Path("/services")
    public List<ArrowheadService> addServices(List<ArrowheadService> serviceList){
    	List<ArrowheadService> savedServices = new ArrayList<ArrowheadService>();
    	for(ArrowheadService service : serviceList){
    		ArrowheadService retrievedService = dm.getArrowheadService(service.getServiceGroup(), 
    				service.getServiceDefinition());
    		if(retrievedService == null){
    			dm.save(service);
    			savedServices.add(service);
    		}
    	}
    	
    	return savedServices;
    }
    
    /**
     * Deletes the ArrowheadService with the id specified by the path parameter.
     * Returns 200 if the delete is succesful, 204 (no content) 
     * if the service was not in the database to begin with.
     * @param {int} - id
     */
    @DELETE
    @Path("/services/{id}")
    public Response deleteService(@PathParam("id") int id){
    	ArrowheadService service = dm.get(ArrowheadService.class, id);
    	if(service == null){
    		return Response.noContent().build();
    	}
    	else{
    		dm.delete(service);
    		return Response.ok().build();
    	}
    }
    
    /**
	 * Returns the list of ArrowheadSystems from the database.
	 *
	 * @return List<ArrowheadSystem>
	 */
    @GET
    @Path("/systems")
    public List<ArrowheadSystem> getAllSystems(){
    	List<ArrowheadSystem> systemList = new ArrayList<ArrowheadSystem>();
    	systemList = dm.getAll(ArrowheadSystem.class);
    	
    	return systemList;
    }
    
    /**
	 * Adds a list of ArrowheadSystems to the database. 
	 * Elements which would cause DuplicateEntryException are being skipped.
	 * The returned list only contains the elements which was saved in the process.
	 *
	 * @param {List<ArrowheadSystem>} systemList 
	 * @return List<ArrowheadSystem>
	 */
    @POST
    @Path("/systems")
    public List<ArrowheadSystem> addSystems(List<ArrowheadSystem> systemList){
    	List<ArrowheadSystem> savedSystems = new ArrayList<ArrowheadSystem>();
    	for(ArrowheadSystem system : systemList){
    		ArrowheadSystem retrievedSystem = dm.getArrowheadSystem(system.getSystemGroup(),
    				system.getSystemName());
    		if(retrievedSystem == null){
    			dm.save(system);
    			savedSystems.add(system);
    		}
    	}
    	
    	return savedSystems;
    }
    
    /**
     * Deletes the ArrowheadSystem with the id specified by the path parameter.
     * Returns 200 if the delete is succesful, 204 (no content) 
     * if the system was not in the database to begin with.
     * @param {int} - id
     */
    @DELETE
    @Path("/systems/{id}")
    public Response deleteSystem(@PathParam("id") int id){
    	ArrowheadSystem system = dm.get(ArrowheadSystem.class, id);
    	if(system == null){
    		return Response.noContent().build();
    	}
    	else{
    		dm.delete(system);
    		return Response.ok().build();
    	}
    }
    
    /**
	 * Returns the list of ArrowheadClouds from the database.
	 *
	 * @return List<ArrowheadCloud>
	 */
    @GET
    @Path("/clouds")
    public List<ArrowheadCloud> getAllClouds(){
    	List<ArrowheadCloud> cloudList = new ArrayList<ArrowheadCloud>();
    	cloudList = dm.getAll(ArrowheadCloud.class);
    	
    	return cloudList;
    }
    
    /**
	 * Adds a list of ArrowheadClouds to the database. 
	 * Elements which would cause DuplicateEntryException are being skipped.
	 * The returned list only contains the elements which was saved in the process.
	 *
	 * @param {List<ArrowheadCloud>} cloudList 
	 * @return List<ArrowheadCloud>
	 */
    @POST
    @Path("/clouds")
    public List<ArrowheadCloud> addClouds(List<ArrowheadCloud> cloudList){
    	List<ArrowheadCloud> savedClouds = new ArrayList<ArrowheadCloud>();
    	for(ArrowheadCloud cloud : cloudList){
    		ArrowheadCloud retrievedCloud = dm.getArrowheadCloud(cloud.getOperator(),
    				cloud.getCloudName());
    		if(retrievedCloud == null){
    			dm.save(cloud);
    			savedClouds.add(cloud);
    		}
    	}
    	
    	return savedClouds;
    }
    
    /**
     * Deletes the ArrowheadCloud with the id specified by the path parameter.
     * Returns 200 if the delete is succesful, 204 (no content) 
     * if the cloud was not in the database to begin with.
     * @param {int} - id
     */
    @DELETE
    @Path("/clouds/{id}")
    public Response deleteCloud(@PathParam("id") int id){
    	ArrowheadCloud cloud = dm.get(ArrowheadCloud.class, id);
    	if(cloud == null){
    		return Response.noContent().build();
    	}
    	else{
    		dm.delete(cloud);
    		return Response.ok().build();
    	}
    }
    
}