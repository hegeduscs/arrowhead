package eu.arrowhead.common;

import javax.ws.rs.NotAllowedException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import eu.arrowhead.common.exception.UnavailableServerException;

public final class Utility {
	
    //private static Client client = ClientBuilder.newClient();
	
	private Utility(){
	}

	public static <T> Response sendRequest(String URI, String method, T payload){
		Response response = null;
		try{
			Client client = ClientBuilder.newClient();
			WebTarget target = client.target(URI);
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
		}
		catch(Exception e){
			 e.printStackTrace();
	         response = null;
		}
		
		if(response == null){
			throw new UnavailableServerException("The server at (" + URI + ") did not respond.");
		}
		
		return response;
	}
}
