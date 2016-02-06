package eu.arrowhead.core.authorization;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import eu.arrowhead.core.authorization.database.ArrowheadCloud;
import eu.arrowhead.core.authorization.database.ArrowheadService;

@Path("authorization")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthorizationResource {
	
	DatabaseManager databaseManager = new DatabaseManager();
	
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "This is the authorization service!";
    }
    
    @GET
    @Path("/operator/{operatorName}")
    //returns a list of Clouds with the same Operator
    public List<ArrowheadCloud> getClouds(@PathParam("operatorName") String operatorName){
    	List<ArrowheadCloud> cloudList = new ArrayList<ArrowheadCloud>();
    	cloudList = databaseManager.getClouds(operatorName);
    	
    	return cloudList;
    }
    
    @GET
    @Path("/operator/{operatorName}/cloud/{cloudName}")
    //returns a Cloud from the database
    public Response getCloud(@PathParam("operatorName") String operatorName, 
    		@PathParam("cloudName") String cloudName){
    	ArrowheadCloud arrowheadCloud = databaseManager.getCloudByName(operatorName, cloudName);
    	return Response.ok(arrowheadCloud).build();
    }
    
    @PUT
    @Path("/operator/{operatorName}/cloud/{cloudName}")
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    //checks whether a Cloud can use a Service (returns with boolean; or with JSON exception)
    public boolean isCloudAuthorized(@PathParam("operatorName") String operatorName, 
    		@PathParam("cloudName") String cloudName, InterCloudAuthRequest request){
    	ArrowheadService requestedService = request.getArrowheadService();
    	ArrowheadCloud arrowheadCloud = databaseManager.getCloudByName(operatorName, cloudName);
    	if(arrowheadCloud == null)
    		return false;
    	boolean isAuthorized = false;
        List<ArrowheadService> retrievedCloudServices = (List<ArrowheadService>) arrowheadCloud.getServiceList();
        for (int j = 0; j < retrievedCloudServices.size(); j++){
         	if(retrievedCloudServices.get(j).isEqual(requestedService))
         		isAuthorized = true; 
         }

    	return isAuthorized;
    }
    
    @POST
    @Path("/operator/{operatorName}/cloud/{cloudName}")
    //adds a new Cloud (and its consumable Services) to the database
    public Response addCloudToAuthorized(@PathParam("operatorName") String operatorName, 
    		@PathParam("cloudName") String cloudName, InterCloudAuthEntry entry, 
    		@Context UriInfo uriInfo){
    	ArrowheadCloud arrowheadCloud = new ArrowheadCloud();
    	arrowheadCloud.setOperator(operatorName);
    	arrowheadCloud.setCloudName(cloudName);
    	arrowheadCloud.setAuthenticationInfo(entry.getAuthenticationInfo());
    	arrowheadCloud.setServiceList(entry.getServiceList());
    	
    	ArrowheadCloud authorizedCloud = databaseManager.addCloudToAuthorized(arrowheadCloud);
    	
    	URI uri = uriInfo.getAbsolutePathBuilder().build();
    	return Response.created(uri).entity(authorizedCloud).build();
    }
    
    @DELETE
    @Path("/operator/{operatorName}/cloud/{cloudName}")
    //deletes a Cloud (and its consumable Services!) from the database
    public Response deleteCloudFromAuthorized(@PathParam("operatorName") String operatorName, 
    		@PathParam("cloudName") String cloudName){
    	databaseManager.deleteCloudFromAuthorized(operatorName, cloudName);
    	
    	return Response.noContent().build();
    }
    
    @GET
    @Path("/operator/{operatorName}/cloud/{cloudName}/services")
    //returns the list of consumable Services of a Cloud
    public List<ArrowheadService> getCloudServices(@PathParam("operatorName") String operatorName, 
    		@PathParam("cloudName") String cloudName){
    	ArrowheadCloud arrowheadCloud = databaseManager.getCloudByName(operatorName, cloudName);
    	List<ArrowheadService> serviceList = (List<ArrowheadService>) arrowheadCloud.getServiceList();
    	
    	return serviceList;
    }
    
    @POST
    @Path("/operator/{operatorName}/cloud/{cloudName}/services")
    //adds a list of consumable Services to a Cloud
    public List<ArrowheadService> addCloudServices(@PathParam("operatorName") String operatorName, 
    		@PathParam("cloudName") String cloudName, InterCloudAuthEntry entry){
    	ArrowheadCloud arrowheadCloud = databaseManager.getCloudByName(operatorName, cloudName);
    	if(!entry.getAuthenticationInfo().isEmpty()){
    		arrowheadCloud.setAuthenticationInfo(entry.getAuthenticationInfo());
    	}
    	List<ArrowheadService> serviceList = (List<ArrowheadService>) entry.getServiceList();
    	arrowheadCloud.getServiceList().addAll(serviceList);
    	databaseManager.updateAuthorizedCloud(arrowheadCloud);
    	
    	return serviceList;
    }
    
    /*
     * Services still in the database after this, needs fixing.
     */
    @PUT
    @Path("/operator/{operatorName}/cloud/{cloudName}/services")
    //list of consumable Services update, deletes the previous list
    public List<ArrowheadService> updateCloudServices(@PathParam("operatorName") String operatorName, 
    		@PathParam("cloudName") String cloudName, InterCloudAuthEntry entry){
    	ArrowheadCloud arrowheadCloud = databaseManager.getCloudByName(operatorName, cloudName);
    	if(!entry.getAuthenticationInfo().isEmpty()){
    		arrowheadCloud.setAuthenticationInfo(entry.getAuthenticationInfo());
    	}
    	List<ArrowheadService> serviceList = (List<ArrowheadService>) entry.getServiceList();
    	arrowheadCloud.getServiceList().clear();
    	arrowheadCloud.getServiceList().addAll(serviceList);
    	databaseManager.updateAuthorizedCloud(arrowheadCloud);
				
    	return serviceList;
    }
    
    /*
     * Services still in the database after this, needs fixing.
     */
    @DELETE
    @Path("/operator/{operatorName}/cloud/{cloudName}/services")
    public Response deleteCloudServices(@PathParam("operatorName") String operatorName, 
    		@PathParam("cloudName") String cloudName, InterCloudAuthEntry entry){
    	//ArrowheadCloud arrowheadCloud = databaseManager.getCloudByName(operatorName, cloudName);
    	List<ArrowheadService> serviceList = (List<ArrowheadService>) entry.getServiceList();
    	System.out.println(serviceList.size());
    	//arrowheadCloud.getServiceList().removeAll(serviceList);
    	databaseManager.deleteServices(operatorName, cloudName, serviceList);
    	
    	return Response.noContent().build();
    }
    
    
}
