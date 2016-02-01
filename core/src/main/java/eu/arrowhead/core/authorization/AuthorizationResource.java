package eu.arrowhead.core.authorization;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import eu.arrowhead.core.authorization.database.ArrowheadCloud;

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
     * Needs to be reworked.
     */
    @GET
    @Path("/operator/{operatorName}/cloud/{cloudName}")
    @Produces(MediaType.TEXT_PLAIN)
    public boolean isCloudAuthorized(@PathParam("operatorName") String operatorName, @PathParam("cloudName") String cloudName){
    	List<ArrowheadCloud> cloudList = new ArrayList<ArrowheadCloud>();
    	cloudList = databaseManager.getCloudByName(operatorName, cloudName);
    	if(cloudList.size() > 0)
    		return true;
    	return false;
    }
    
    @POST
    @Path("/operator/{operatorName}/cloud/{cloudName}")
    public Response addCloudToAuthorized(@PathParam("operatorName") String operatorName, 
    		@PathParam("cloudName") String cloudName, AuthorizationRequest request, 
    		@Context UriInfo uriInfo){
    	ArrowheadCloud arrowheadCloud = new ArrowheadCloud();
    	arrowheadCloud.setOperator(operatorName);
    	arrowheadCloud.setCloudName(cloudName);
    	arrowheadCloud.setAuthenticationInfo(request.getAuthenticationInfo());
    	arrowheadCloud.setServiceList(request.getServiceList());
    	
    	ArrowheadCloud authorizedCloud = databaseManager.addCloudToAuthorized(arrowheadCloud);
    	
    	URI uri = uriInfo.getAbsolutePathBuilder().build();
    	return Response.created(uri).entity(authorizedCloud).build();
    }
}
