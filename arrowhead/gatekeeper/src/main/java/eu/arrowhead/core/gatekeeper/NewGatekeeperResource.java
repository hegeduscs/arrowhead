package eu.arrowhead.core.gatekeeper;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import eu.arrowhead.common.Utility;
import eu.arrowhead.common.configuration.SysConfig;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.messages.GSDAnswer;
import eu.arrowhead.common.model.messages.GSDPoll;
import eu.arrowhead.common.model.messages.GSDRequestForm;
import eu.arrowhead.common.model.messages.GSDResult;

/**
 * @author umlaufz
 */
@Path("gatekeeper")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class NewGatekeeperResource {
	
	//private static Logger log = Logger.getLogger(GatekeeperResource.class.getName());
	
	@GET
    public String getIt() {
	    return "This is the Gatekeeper Resource stub.";
    }
	
	/**
	 * todo commenting
	 * @param requestForm
	 * @return
	 */
	@PUT
	@Path("init_gsd")
	public Response GSDRequest(GSDRequestForm requestForm){
		
		if(!requestForm.isPayloadUsable()){
			throw new BadPayloadException("Bad payload: missing/incomplete requestedService."
					+ "Mandatory fields: serviceGroup, serviceDefinition, interfaces.");
		}
		
		ArrowheadCloud ownCloud = SysConfig.getOwnCloud();
		GSDPoll gsdPoll = new GSDPoll(requestForm.getRequestedService(), ownCloud);
		List<String> cloudURIs = new ArrayList<String>();
		cloudURIs = SysConfig.getCloudURIs();
		
		List<GSDAnswer> gsdAnswerList = new ArrayList<GSDAnswer>();
		for(String URI : cloudURIs){
			Response response = Utility.sendRequest(URI, "PUT", gsdPoll);
			GSDAnswer gsdAnswer = response.readEntity(GSDAnswer.class);
			if(gsdAnswer != null){
				gsdAnswerList.add(gsdAnswer);
			}
		}
		
		GSDResult gsdResult = new GSDResult(gsdAnswerList);
		
		return Response.ok().entity(gsdResult).build();
	}

}
