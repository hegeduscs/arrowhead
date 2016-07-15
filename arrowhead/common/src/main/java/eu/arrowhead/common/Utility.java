package eu.arrowhead.common;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import eu.arrowhead.common.exception.ErrorMessage;

public final class Utility {
	
    private static Client client = ClientBuilder.newClient();
	
	private Utility(){
	}

	public static <T> Response sendRequest(String URI, String method, T payload){
		WebTarget target = client.target(URI);
		switch(method){
			case "GET": 
				return target.request().header("Content-type", "application/json").get();
			case "POST":
				return target.request().header("Content-type", "application/json").post(Entity.json(payload));
			case "PUT":
				return target.request().header("Content-type", "application/json").put(Entity.json(payload));
			case "DELETE":
				return target.request().header("Content-type", "application/json").delete();
			default:{
				ErrorMessage errorMessage = new ErrorMessage("Invalid method type was given "
						+ "to the Utility.sendRequest() method", 405, "No documentation yet.");
				return Response.status(Status.METHOD_NOT_ALLOWED).entity(errorMessage).build();
			}
		}
	}
}
