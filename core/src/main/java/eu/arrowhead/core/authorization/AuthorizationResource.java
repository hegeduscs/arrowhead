package eu.arrowhead.core.authorization;

import java.net.URI;
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
import javax.ws.rs.core.Response.Status;

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
    
    @PUT
    @Path("/operator/{operatorName}/cloud/{cloudName}")
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    //can check whether a Cloud can use a Service (returns with boolean; or with JSON exception)
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
    //can add new Cloud (and its consumable Services) to the database
    //or override the whole auth entry
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
    //deletes a Cloud (and its Services!) from the database
    public Response deleteCloudFromAuthorized(@PathParam("operatorName") String operatorName, 
    		@PathParam("cloudName") String cloudName){
    	databaseManager.deleteCloudFromAuthorized(operatorName, cloudName);
    	
    	return Response.status(Status.OK)
				.build();
    }
    
    
}
