package eu.arrowhead.core.authorization;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("authorization")
public class AuthorizationResource {
	
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "This is the authorization service!";
    }
    
    @GET
    @Path("/operator/{operatorId}/cloud/{cloudId}")
    @Produces(MediaType.TEXT_PLAIN)
    public boolean isAuthorized(@PathParam("operatorId") String operatorId, @PathParam("cloudId") String cloudId){
    	
    	return true;
    }
}
