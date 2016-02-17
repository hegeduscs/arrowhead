package eu.arrowhead.core.gatekeeper;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
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
	public ArrowheadService requestedService;
	public ArrowheadCloud requesterCloud;
	List<ProvidedService> testservices = new ArrayList<ProvidedService>();
	ProvidedService providedService;

	
	@GET
//    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "This is the Gatekeeper Resource stub.";
    }
    
    @PUT
//    @Path("/init_gsd/1")
    @Path("/init_gsd/")
    //XXX: Testing
    public GSDAnswer sendRequest(GSDPoll gsdPollRequest){
//    	GSDPoll gsdPoll = gsdPollRequest;
    	
    	//Test GSDPoll
    	List<String> interfaces = new ArrayList<String>();
		interfaces.add("test1");
		interfaces.add("test2");
    	requestedService = new ArrowheadService("serviceGroup", "serviceDefinition", interfaces, "metaData");
    	requesterCloud = new ArrowheadCloud("operator", "cloudName", "localhost", "8080", "gkURI", "authInfo");
    	GSDPoll gsdPoll = new GSDPoll(requestedService, requesterCloud);    	

    	Client client = ClientBuilder.newClient();
    	String uri = "http://localhost:8080/core/gatekeeper/gsd_poll/";
//    	String uri = "http://localhost:8080/core/gatekeeper/gsd_poll/2";
		WebTarget target = client.target(uri);
	    Response response = target
	    		.request()
	    		.header("Content-type", "application/json")
				.put(Entity.json(gsdPoll));
	    return response.readEntity(GSDAnswer.class);
	    
    }
    
    @PUT
//    @Path("/gsd_poll/2")
    @Path("/gsd_poll/")
    public GSDAnswer getRequest(GSDPoll gsdPollRequest){
    	System.out.println("getRequest method");
    	GSDPoll gsdPoll = gsdPollRequest;    	

    	ArrowheadService requester = gsdPoll.getRequestedService();
    	String cloudOperator = gsdPoll.getRequesterCloud().getOperator();
    	String cloudName = gsdPoll.getRequesterCloud().getName(); 
    	String uri = "http://localhost:8080/core/authorization/operator/"+cloudOperator+"/cloud/"+cloudName;
    	System.out.println(uri);
    	
    	//No Author
    	ArrowheadSystem requesterSystem = new ArrowheadSystem("systemGroup", "systemName", "iPAddress", "port", "authenticationInfo");
    	ServiceRequestForm srf = new ServiceRequestForm(requester, "requestedQoS", requesterSystem, 1000); //TODO: servicingLength
    	ServiceQueryForm sqf = new ServiceQueryForm(srf);
    	System.out.println("eljutok idáig?");
    	System.out.println(sqf.getServiceInterfaces());

    	ServiceQueryResult sqr = getServiceQueryResult(sqf); //TODO: ServiceRequestForm
    	GSDAnswer answer = new GSDAnswer(sqr.getServiceQueryData(), gsdPoll.getRequesterCloud()); //FIXME: ProviderCloud
    	System.out.println(answer.getAnswer().toString());
    	return answer;

//    	Client client = ClientBuilder.newClient();
//		WebTarget target = client.target(uri); //FIXME: bad URI?
//	    Response response = target
//	    		.request()
//	    		.header("Content-type", "application/json")
//				.put(Entity.json(gsdPoll)); 
//    	System.out.println("mizu?");
//
//	    if(response.readEntity(Boolean.class)){
//	    	ArrowheadSystem requesterSystem = new ArrowheadSystem("systemGroup", "systemName", "iPAddress", "port", "authenticationInfo");
//	    	ServiceRequestForm srf = new ServiceRequestForm(requester, "requestedQoS", requesterSystem, 1000);
//	    	ServiceQueryForm sqf = new ServiceQueryForm(srf);
//	    	System.out.println("eljutok idáig?");
//	    	System.out.println(sqf.getServiceInterfaces());
//
//	    	ServiceQueryResult sqr = getServiceQueryResult(sqf);
//	    	GSDAnswer answer = new GSDAnswer(sqr.getServiceQueryData(), gsdPoll.getRequesterCloud());
//	    	System.out.println(answer.getAnswer().toString());
//	    	return answer;
//	    }else return null; //XXX: Error handling
    }
    
    /**
     * WORK IN PROGRESS
     */
    /*
    @GET
    @Path("/init_icn/1")
    //Work in progress
    public GSDAnswer sendProposal(ICNProposal icnProposal){
    	ICNProposal proposal = icnProposal;    	
    	
    	Client client = ClientBuilder.newClient();
		WebTarget target = client.target("http://localhost:8080/core/gatekeeper/icn_proposal/2");
	    Response response = target
	    		.request()
	    		.accept(MediaType.APPLICATION_JSON)
	    		.header("Content-type", "application/json")
	    		.put(Entity.json(proposal)); 
	    return response.readEntity(GSDAnswer.class);
    }
    
    //Work in progress
    @GET
    @Path("/icn_proposal/2")
    public OrchestrationForm ICNEnd (ICNProposal icnProposal){
    	OrchestrationForm icnEnd = new OrchestrationForm();
		return icnEnd;
    }
    */
    
    
  //Copy from eu.arrowhead.core.orchestrator.OrchestrationResource
  	private ServiceQueryResult getServiceQueryResult(ServiceQueryForm sqfARG/*, URI uri*/) {
  		ServiceQueryForm sqf = sqfARG;
  		System.out.println("GK: inside the getServiceQueryResult function");
  		Client client = ClientBuilder.newClient();
  		// WebTarget target = client.target(uri);
  		WebTarget target = client.target("http://localhost:8080/ext/serviceregistry/query");
  		Response response = target.request().header("Content-type", "application/json").put(Entity.json(sqf));
  		System.out.println("GK: gSQR received the response");

// 		ServiceQueryResult sqr = response.readEntity(ServiceQueryResult.class); //FIXME
  		
  		//fixme helyett:
  		ArrowheadSystem provider = new ArrowheadSystem("a", "g", "f", "fd", "dd");
		providedService = new ProvidedService(provider , "serviceURI", "serviceInterface");
  		testservices.add(providedService);
  		ServiceQueryResult sqr = new ServiceQueryResult(testservices);
  		
  		
  		System.out.println("GK received the following serviceURIs:");
  		for (ProvidedService providedService : sqr.getServiceQueryData()) {
  			System.out.println(providedService.getServiceURI() + providedService.getProvider().getIPAddress());
  		}
  		return sqr;
  	}
    
    
    
    
    
    
}