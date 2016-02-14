package eu.arrowhead.core.authorization;

import java.net.URI;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.model.messages.InterCloudAuthRequest;
import eu.arrowhead.common.model.messages.IntraCloudAuthRequest;
import eu.arrowhead.common.model.messages.IntraCloudAuthResponse;
import eu.arrowhead.core.authorization.database.ArrowheadCloud;
import eu.arrowhead.core.authorization.database.ArrowheadService;
import eu.arrowhead.core.authorization.database.ArrowheadSystem;
import eu.arrowhead.core.authorization.database.Systems_Services;

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
    
    @PUT
    @Path("/systemgroup/{systemGroup}/system/{systemName}")
    public IntraCloudAuthResponse isSystemAuthorized(@PathParam("systemGroup") String systemGroup,
    		@PathParam("systemName") String systemName, IntraCloudAuthRequest request){
    	Systems_Services ss = new Systems_Services();
    	HashMap<ArrowheadSystem, Boolean> authorizationMap = new HashMap<ArrowheadSystem, Boolean>();
    	IntraCloudAuthResponse response = new IntraCloudAuthResponse();
    	ArrowheadSystem consumer = databaseManager.getSystemByName(systemGroup, systemName);
    	if(consumer == null){
    		throw new DataNotFoundException("The Consumer System is not found in the database.");
    	}
    	
    	//with this solution the extra payload information is useless, only the SG and SD matters
    	//meaning the interfaces might not have a match!
    	List<ArrowheadService> serviceList = new ArrayList<ArrowheadService>();
    	serviceList = databaseManager.getServiceByName(request.getArrowheadService()
    			.getServiceGroup(), request.getArrowheadService().getServiceDefinition());
    	if(serviceList.isEmpty()){
    		for(ArrowheadSystem provider : request.getProviderList()){
    			authorizationMap.put(provider, false);
    		}
    		response.setAuthorizationMap(authorizationMap);
    		return response;
    	}
    	
    	for(ArrowheadSystem provider : request.getProviderList()){
    		ArrowheadSystem retrievedSystem = databaseManager.getSystemByName(provider.getSystemGroup(), 
    				provider.getSystemName());
    		ss = databaseManager.getSS(consumer, retrievedSystem, serviceList.get(0));
        	if(ss == null){
        		authorizationMap.put(provider, false);
        	}
        	else{
        		authorizationMap.put(retrievedSystem, true);
        	}
    	}
    	
    	
    	response.setAuthorizationMap(authorizationMap);
    	return response;
    }
    
    /*
     * duplicate entry exception when the same providersystems are in the payload of 2 different request!!
     */
    @POST
    @Path("/systemgroup/{systemGroup}/system/{systemName}")
    public Response addSystemToAuthorized(@PathParam("systemGroup") String systemGroup,
    		@PathParam("systemName") String systemName, IntraCloudAuthEntry entry){
    	ArrowheadSystem consumerSystem = databaseManager.getSystemByName(systemGroup, systemName);
    	if(consumerSystem == null){
    		ArrowheadSystem consumer = new ArrowheadSystem();
    		consumer.setSystemGroup(systemGroup);
    		consumer.setSystemName(systemName);
    		consumer.setIPAddress(entry.getIPAddress());
    		consumer.setPort(entry.getPort());
    		consumer.setAuthenticationInfo(entry.getAuthenticationInfo());
    		consumerSystem = databaseManager.save(consumer);
    	}
    	
    	
    	ArrowheadSystem retrievedSystem = null;
    	List<ArrowheadService> retrievedServiceList = new ArrayList<ArrowheadService>();
    	Systems_Services ss = new Systems_Services();
    	
    	for (ArrowheadSystem providerSystem : entry.getProviderList()){
    		retrievedSystem = databaseManager.getSystemByName(providerSystem.getSystemGroup(), 
    				providerSystem.getSystemName());
    		if(retrievedSystem == null){
    			databaseManager.save(providerSystem);
    		}
    		for(ArrowheadService service : entry.getServiceList()){
    			retrievedServiceList = databaseManager.getServiceByName(service.getServiceGroup(), 
    					service.getServiceDefinition());
    			if(retrievedServiceList.isEmpty()){
    				databaseManager.save(service);
    			}
    			ss.setConsumer(consumerSystem);
    			ss.setProvider(providerSystem);
    			ss.setService(service);
    			databaseManager.saveRelation(ss);
    		}
    	}
    	
    	return Response.status(Status.CREATED).entity(consumerSystem).build();
    }
    
    @DELETE
    @Path("/systemgroup/{systemGroup}/system/{systemName}")
    public Response deleteRelationsFromAuthorized(@PathParam("systemGroup") String systemGroup,
    		@PathParam("systemName") String systemName){
    	ArrowheadSystem consumer = databaseManager.getSystemByName(systemGroup, systemName);
    	List<Systems_Services> ssList = new ArrayList<Systems_Services>();
    	ssList = databaseManager.getRelations(consumer);
    	for(Systems_Services ss : ssList){
    		databaseManager.delete(ss);
    	}
    	
    	return Response.noContent().build();
    }
    
}
