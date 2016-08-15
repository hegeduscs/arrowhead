package eu.arrowhead.common;

import javax.ws.rs.NotAllowedException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;

import org.apache.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import eu.arrowhead.common.exception.UnavailableServerException;
import eu.arrowhead.common.ssl.SecurityUtils;

public final class Utility {
	
	private static Logger log = Logger.getLogger(Utility.class.getName());
	
	private Utility(){
	}

	public static <T> Response sendRequest(String URI, String method, T payload){
		log.info("Sending " + method + " request to: " + URI);
		
		Response response = null;
		try{
		    ClientConfig configuration = new ClientConfig();
		    configuration.property(ClientProperties.CONNECT_TIMEOUT, 30000);
		    configuration.property(ClientProperties.READ_TIMEOUT, 30000);
		    Client client = ClientBuilder.newClient(configuration);

		    WebTarget target = client.target(UriBuilder.fromUri(URI).build());
		    switch(method){
		    case "GET": 
		        response = target.request().header("Content-type", "application/json").get();
		        break;
		    case "POST":
		        response = target.request().header("Content-type", "application/json").post(Entity.json(payload));
		        break;
		    case "PUT":
		        response = target.request().header("Content-type", "application/json").put(Entity.json(payload));
		        break;
		    case "DELETE":
		        response = target.request().header("Content-type", "application/json").delete();
		        break;
		    default:
		        throw new NotAllowedException("Invalid method type was given "
		                + "to the Utility.sendRequest() method");
		    }
		    
		    return response;
		}
		catch(Exception e){
		    e.printStackTrace();
		    //Internal Server Error, Not Found
		    if(response == null || response.getStatus() == 500 || response.getStatus() == 404){
		    	log.info("UnavailableServerException at " + URI);
		        throw new UnavailableServerException("Server(s) timed out. Check logs for details.");
		    }
		}
		
		return Response.status(Status.NOT_FOUND).build();
	}
	
	public static boolean isClientAuthorized(SecurityContext sc, Configuration configuration){
		String subjectname = sc.getUserPrincipal().getName();
		System.out.println("Received message with subject: " + subjectname);
		String clientCN = SecurityUtils.getCertCNFromSubject(subjectname);
		System.out.println("Client CN: " + clientCN);
		String serverCN = (String) configuration.getProperty("server_common_name");
		System.out.println("Server CN: " + serverCN);
		
		String[] cnFields = serverCN.split("\\.", -1);
		String allowedCN = null;
		if(cnFields.length < 3){
			//error, serverCN is shorter than it should be
			return false;
		}
		else{
			allowedCN = "orchestrator.coresystems";
			for(int i = 2; i < cnFields.length; i++){
				allowedCN = allowedCN.concat(cnFields[i]);
			}
		}
		
		System.out.println(allowedCN);
		if(!clientCN.equalsIgnoreCase(allowedCN)){
			return false;
		}
		
		return true;
	}
	
	
}
