package eu.arrowhead.common.ssl;

import eu.arrowhead.common.exception.AuthenticationException;

import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.security.auth.x500.X500Principal;
import javax.servlet.http.HttpServletRequest;

public class SSLAuthentication {

	public SSLAuthentication(){
		
	}

    public String getIt(HttpServletRequest request) {
    	
    	// We extract the certificate list from the HTTP request;
    	// This contains multiple certs up in the hierarchy of trust;
    	// The first cert in the list is the client's cert, we need that;
    	X509Certificate certs[] = (X509Certificate[])request.getAttribute("javax.servlet.request.X509Certificate");
    	if(certs == null) 
    	{
    		throw new AuthenticationException("Error: no certificate received in request!");
    	}

    	//TODO rethink cert CN structure
    	// We use the certificate subject field for identifying the sender;
    	// Certificate subject field looks like this:
    	//  CN=arrowhead.cloud1.client, O=Arrowhead, L=Budapest, C=HU
    	//  CN is 'Common Name' which can be used for authentication
    	//	  We need to extract it!
    	X500Principal subjectDN = certs[0].getSubjectX500Principal();
    	String subjectname = subjectDN.getName(X500Principal.RFC1779);
    	
    	String cn = null;
    	try {
    		// Subject is in LDAP format, we can use the LdapName object for parsing;
			LdapName ldapname = new LdapName(subjectname);
			for(Rdn rdn : ldapname.getRdns()) {
				// Find the data after the CN field
				if(rdn.getType().equalsIgnoreCase("CN"))
					cn = (String)rdn.getValue();
			}
		} catch (InvalidNameException e1) {
			throw new AuthenticationException(e1.getMessage());
		}
    	
    	if(cn == null) {
    		throw new AuthenticationException("Error: certificate subject does not contain CN field!");
    	}
    	
    	// We extract the strings between the dots
    	String[] cnfields = cn.split("\\.");
    	
    	if(cnfields.length != 3) {
    		throw new AuthenticationException("Error: certificate subject CN fields should be three string separated by dot characters!");
    	}
    	
    	if(!cnfields[0].equalsIgnoreCase("arrowhead")) {
    		throw new AuthenticationException("Error: request is not from an arrowhead client...!");
    	}
    	
    	// Check if client is in the same cloud as we are...
    	if(!cnfields[1].equalsIgnoreCase("cloud1")) {
    		// TODO: maybe send the message to the gatekeeper?
    		throw new AuthenticationException("Client is not in our cloud...");    		
    	}
    	
    	// TODO:sanyi- authorize based on cnfields[3];
    	
    	String msg = "We received " + certs.length + " certificates from subject: " + subjectname + ", CN: " + cn + ", array: " + Arrays.toString(cnfields);
    	return msg;     	
    }
}
