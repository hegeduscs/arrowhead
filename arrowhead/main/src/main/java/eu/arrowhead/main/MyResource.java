package eu.arrowhead.main;

import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.security.auth.x500.X500Principal;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Context;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("myresource")
public class MyResource {

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt(@Context SecurityContext sc) {  	
    	System.out.println("Request received!");
      	// We extract the certificate list from the HTTP request;
    	// This contains multiple certs up in the hierarchy of trust;
    	// The first cert in the list is the client's cert, we need that;
    	
    	if(sc == null) return "Error: SecurityContext is null!";
    	
    	String name = "Received User id: " + sc.getUserPrincipal().getName();
    	System.out.println(name);
    	return name;


    }
}
