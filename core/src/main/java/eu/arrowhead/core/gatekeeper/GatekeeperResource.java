package eu.arrowhead.core.gatekeeper;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.messages.GSDAnswer;
import eu.arrowhead.common.model.messages.GSDPoll;
import eu.arrowhead.common.model.messages.GSDRequestForm;
import eu.arrowhead.common.model.messages.GSDResult;
import eu.arrowhead.common.model.messages.ICNProposal;
import eu.arrowhead.common.model.messages.OrchestrationForm;
import eu.arrowhead.common.model.messages.ProvidedService;
import eu.arrowhead.common.model.messages.QoSVerificationResponse;
import eu.arrowhead.common.model.messages.QoSVerify;
import eu.arrowhead.common.model.messages.ServiceQueryForm;
import eu.arrowhead.common.model.messages.ServiceQueryResult;
import eu.arrowhead.common.model.messages.ServiceRequestForm;

/**
 * 
 * @author blevente92
 *
 */
@Path("gatekeeper")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GatekeeperResource {
	private GSDRequestForm exampleRequest;
//	private ArrowheadSystem requesterSystem;
	private ArrowheadService requestedService;
	private ArrowheadCloud requesterCloud;
		
	
    @GET
//    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
		List<String> interfaces = new ArrayList<String>();
		interfaces.add("test1");
		interfaces.add("test2");
    	requestedService = new ArrowheadService("serviceGroup", "serviceDefinition", interfaces, "metaData");
    	requesterCloud = new ArrowheadCloud("op", "clN", "gkIP", "gkPort", "gkURI", "authInfo");
    	exampleRequest = new GSDRequestForm(requestedService);
        return "This is the Gatekeeper Resource stub.";
    }
    
    @PUT
    @Path("/init_gsd/1")
    //TODO: Testing
    public GSDAnswer sendRequest(GSDPoll gsdPollRequest){
    	GSDPoll gsdPoll = new GSDPoll(requestedService, requesterCloud);    	
    	Client client = ClientBuilder.newClient();
    	String uri = "http://localhost:8080/core/gatekeeper/gsd_poll/2";
		WebTarget target = client.target(uri);
	    Response response = target
	    		.request()
	    		.header("Content-type", "application/json")
				.put(Entity.json(gsdPoll));
	    return response.readEntity(GSDAnswer.class);
    }
    
    @PUT
    @Path("/gsd_poll/2")
    public GSDAnswer getRequest(GSDPoll gsdPoll){
    	System.out.println("getRequest method");
    	ArrowheadService requester = gsdPoll.getRequestedService();
    	String cloudOperator = gsdPoll.getRequesterCloud().getOperator();
    	String cloudName = gsdPoll.getRequesterCloud().getName(); //FIXME: Provider Cloud kell ??
    	String uri = "http://localhost:8080/core/authorization/operator/"+cloudOperator+"/cloud/"+cloudName;
    	Client client = ClientBuilder.newClient();
		WebTarget target = client.target(uri);
	    Response response = target
	    		.request()
	    		.header("Content-type", "application/json")
				.put(Entity.json(gsdPoll)); //FIXME: argument
	    if(response.readEntity(Boolean.class)){
	    	ArrowheadSystem requesterSystem = new ArrowheadSystem(); //TODO
	    	ArrowheadCloud providerCloud = new ArrowheadCloud(); //TODO
	    	ServiceRequestForm srf = new ServiceRequestForm(requester, "requestedQoS", requesterSystem, 1000); //FIXME: arguments
	    	ServiceQueryForm sqf = new ServiceQueryForm(srf);//FIXME: ServiceRequestForm
	    	//TODO: SRF to Orch
	    	ServiceQueryResult sqr = getServiceQueryResult(sqf);	    	
	    	GSDAnswer answer = new GSDAnswer(sqr.getServiceQueryData(), providerCloud); //FIXME: providerCloud
	    	return answer;
	    }else return null; //Error handling
    }
    
    
    
    
    
    /**
     * WORK IN PROGRESS
     */
    
    /*
    @GET
    @Path("/init_icn/1")
    //TODO: Work in progress
    public GSDAnswer sendProposal(ICNProposal icnProposal){
    	ICNProposal proposal = icnProposal;    	
    	
    	Client client = ClientBuilder.newClient();
		WebTarget target = client.target("http://localhost:8080/core/gatekeeper/icn_proposal/2");
	    Response response = target
	    		.request()
	    		.accept(MediaType.APPLICATION_JSON)
	    		.header("Content-type", "application/json")
	    		.put(Entity.json(proposal)); //TODO
	    return response.readEntity(GSDAnswer.class); //TODO 
    }
    
    
    
    //TODO: Work in progress
    @GET
    @Path("/icn_proposal/2")
    public OrchestrationForm ICNEnd (ICNProposal icnProposal){
    	OrchestrationForm icnEnd = new OrchestrationForm();
		return icnEnd;
    }
    
    
    */
    
    
    
    
  //Copy from eu.arrowhead.core.orchestrator.OrchestrationResource
  	private ServiceQueryResult getServiceQueryResult(ServiceQueryForm sqf/*, URI uri*/) {
  		System.out.println("orchestator: inside the getServiceQueryResult function");
  		Client client = ClientBuilder.newClient();
  		// WebTarget target = client.target(uri);
  		WebTarget target = client.target("http://localhost:8080/ext/serviceregistry/query");
  		Response response = target.request().header("Content-type", "application/json").put(Entity.json(sqf));
  		System.out.println("orchestrator: gSQR received the response");
  		ServiceQueryResult sqr = response.readEntity(ServiceQueryResult.class);
  		System.out.println("orchestrator received the following serviceURIs:");
  		for (ProvidedService providedService : sqr.getServiceQueryData()) {
  			System.out.println(providedService.getServiceURI() + providedService.getProvider().getIPAddress());
  		}
  		return sqr;
  	}
    
    
    
    
    
    
}