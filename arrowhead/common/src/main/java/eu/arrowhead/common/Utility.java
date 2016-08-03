package eu.arrowhead.common;

import javax.ws.rs.NotAllowedException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.apache.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import eu.arrowhead.common.exception.UnavailableServerException;

public final class Utility {
	
	private static Logger log = Logger.getLogger(Utility.class.getName());
	
	private Utility(){
	}

	public static <T> Response sendRequest(String URI, String method, T payload){
		log.info("Sending " + method + " request to: " + URI);
		
		Response response = null;
		try{
			//TODO uncomment when finished testing? also test the behaviour of it
		    /*ClientConfig configuration = new ClientConfig();
		    configuration.property(ClientProperties.CONNECT_TIMEOUT, 10000);
		    configuration.property(ClientProperties.READ_TIMEOUT, 10000);*/
		    Client client = ClientBuilder.newClient();

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
		    
		    //TODO check if this actually returns the URI we want
		    //Service Unavailable
		    if(response.getStatus() == 503){
		    	log.info("UnavailableServerException at " + URI);
		    	throw new UnavailableServerException("The server at (" + URI + ") did not respond.");
		    }
		    
		    return response;
		}
		//We need to catch this exception separately, so we can get back the original URI that was unavailable
		catch(UnavailableServerException e){
			e.printStackTrace();
			throw new UnavailableServerException(e.getMessage());
		}
		//This catches the JAX-RS exception which is actually thrown when trying to send to an invalid URI
		catch(Exception e){
		    e.printStackTrace();
		    //Internal Server Error, Not Found
		    if(response == null || response.getStatus() == 500 || response.getStatus() == 404){
		    	log.info("UnavailableServerException at " + URI);
		        throw new UnavailableServerException("The server at (" + URI + ") did not respond.");
		    }
		}
		
		return Response.status(Status.NOT_FOUND).build();
	}
}
