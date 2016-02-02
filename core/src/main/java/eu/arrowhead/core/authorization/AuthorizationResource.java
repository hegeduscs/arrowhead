package eu.arrowhead.core.authorization;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
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
    
    /*
     * Not working as intended yet.
     */
    @PUT
    @Path("/operator/{operatorName}/cloud/{cloudName}")
    @Produces(MediaType.TEXT_PLAIN)
    public boolean isCloudAuthorized(@PathParam("operatorName") String operatorName, 
    		@PathParam("cloudName") String cloudName, InterCloudAuthRequest request){
    	ArrowheadService requestedService = request.getArrowheadService();
    	List<ArrowheadCloud> cloudList = new ArrayList<ArrowheadCloud>();
    	cloudList = databaseManager.getCloudByName(operatorName, cloudName);
    	boolean isAuthorized = false;
    	for (int i = 0; i < cloudList.size(); i++) {
         	List<ArrowheadService> retrievedcloudServices=(List<ArrowheadService>)cloudList.get(i).getServiceList();
         	//System.out.println(retrievedcloudServices.size());
         	for (int j = 0; j < retrievedcloudServices.size(); j++){
         		 if(retrievedcloudServices.get(j).isEqual(requestedService))
         			//System.out.println(retrievedcloudServices.get(j).getServiceDefinition());
         			isAuthorized = true;
         	}
         }
    	//System.out.println(cloudList.get(0).getAuthenticationInfo() + cloudList.get(0).getCloudName() + cloudList.get(0).getOperator());
    	return isAuthorized;
    }
    
    @POST
    @Path("/operator/{operatorName}/cloud/{cloudName}")
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
}
