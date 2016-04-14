package eu.arrowhead.main;

import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.jersey.server.ResourceConfig;


/**
 * Main class.
 *
 */
public class Main {

    /**
     * Main method.
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
    	    	    	    	    		
		SSLContextConfigurator sslCon = new SSLContextConfigurator();
	        sslCon.setKeyStoreFile("/home/sanyi/Development/certs/cloud1.aaaservice/cloud1.aaa.jks");
	        sslCon.setKeyStorePass("123456");
	        sslCon.setTrustStoreFile("/home/sanyi/Development/certs/master/mastercacerts.jks");
	        sslCon.setTrustStorePass("123456"); 
        
        ServerInfo authServer = new ServerInfo(
        		"authorization",8444,
        		new ResourceConfig().registerClasses(
        				SecurityFilter.class,
        				eu.arrowhead.core.authorization.AuthorizationResource.class)
        		).setSSLContext(sslCon);
        
        ServerInfo testServer = new ServerInfo(
        		"myapp",8443,
        		new ResourceConfig().registerClasses(
        				SecurityFilter.class,
        				MyResource.class))
        		.setSSLContext(sslCon);        
        
        
        testServer.start();
        authServer.start();
        System.out.println("Press enter to exit...");
        System.in.read();
        //authServer.stop();
        testServer.stop();
        
    }
}

