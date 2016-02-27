package eu.arrowhead.common.ssl;

import javax.net.ssl.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.SslConfigurator;

/**
 * Test class to showcase the use of SSL. Parts of the code has to be modified to match workspace states.
 */

public class SSLClient 
{
    public static void main( String[] args )
    { 	
		try {
			//TODO modify file paths pointing to the keystore files
			SslConfigurator sslConfig = SslConfigurator.newInstance()
			        .trustStoreFile("C:\\tomcat8_2\\conf\\mastercacerts.jks")
			        .trustStorePassword("123456")
			        .keyStoreFile("C:\\tomcat8_2\\conf\\cloud1.client1.jks")
			        .keyPassword("123456");
			
			SSLContext sslContext = sslConfig.createSSLContext();
			
	        HostnameVerifier allHostsValid = new HostnameVerifier() {
	            public boolean verify(String hostname, SSLSession session) {
	                System.out.println("Verifying host: " + hostname);
	                try {
						java.security.cert.Certificate[] certs = session.getPeerCertificates();
						System.out.println("Response contains " + certs.length + " certificates");
						/*for(int i=0;i<certs.length;i++)
						{
							System.out.println("Certificate " + i + " contains:");
							System.out.println(certs[i].toString());
						}*/
						
					} catch (SSLPeerUnverifiedException e) {
						System.out.println("Error: client's identity was not verified!");
					}
	            	return true;
	            }
	        }; 			
			
	    	Client c = ClientBuilder.newBuilder().sslContext(sslContext).hostnameVerifier(allHostsValid).build();
	    	
	    	//TODO modify WebTarget + HTTP method type according to usage
	    	WebTarget target = c.target("https://localhost:8443/core/");
	    	String responseMsg = target.path("orchestrator").request().get(String.class);
	        System.out.println( "Response: " +  responseMsg);			
			
		} catch (Exception e) {			
			e.printStackTrace();
		}		
    }
}
