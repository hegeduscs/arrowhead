package eu.arrowhead.common;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
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

import eu.arrowhead.common.exception.AuthenticationException;
import eu.arrowhead.common.exception.UnavailableServerException;

public final class Utility {
	
	private static Logger log = Logger.getLogger(Utility.class.getName());
	private static SSLContext sslContext = null;
	
	private Utility(){
	}
	
	public static void setSSLContext (SSLContext context) {
        sslContext = context;
    }
	
	public static HostnameVerifier allHostsValid = new HostnameVerifier() {
		public boolean verify(String hostname, SSLSession session) {
			// Decide whether to allow the connection...
			return true;
		}
	};

	public static <T> Response sendRequest(String URI, String method, T payload){
		log.info("Sending " + method + " request to: " + URI);
		
		Response response = null;
		boolean isSecure = false;
		if(URI.startsWith("https")){
			isSecure = true;
		}
		
		try{
		    ClientConfig configuration = new ClientConfig();
		    configuration.property(ClientProperties.CONNECT_TIMEOUT, 30000);
		    configuration.property(ClientProperties.READ_TIMEOUT, 30000);
		    
		    Client client = null;
            if (isSecure && Utility.sslContext != null) {
                client = ClientBuilder.newBuilder().sslContext(sslContext).withConfig(configuration)
                        .hostnameVerifier(allHostsValid).build();
            } else if (isSecure && Utility.sslContext == null) {
                throw new AuthenticationException("SSL Context not set, but secure was invoked.");
            } else {
                client = ClientBuilder.newClient(configuration);
            }
            
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
	
	
}
