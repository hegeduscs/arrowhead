package eu.arrowhead.main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    	
    	CertificateHandler certHandler = new CertificateHandler("/home/sanyi/Development/certs")
    			.setTrustStore("mastercacerts")
    			.setDefaultPassword("123456");
    	
    	List<ServerInfo> serverList = new ArrayList<ServerInfo>();
    	
    	serverList.add(new ServerInfo(
        		"serviceregistry",8444,
        		new ResourceConfig().registerClasses(
        				SecurityFilter.class,
        				eu.arrowhead.core.serviceregistry.ServiceRegistryResource.class)
        		).setSSLContext(certHandler.getSSLContext("cloud1.serviceregistry")));
                
        
    	serverList.add(new ServerInfo(
        		"authorization",8445,
        		new ResourceConfig().registerClasses(
        				SecurityFilter.class,
        				eu.arrowhead.core.authorization.AuthorizationResource.class)
        		).setSSLContext(certHandler.getSSLContext("cloud1.authorization")));
              
        
    	serverList.add(new ServerInfo(
        		"gatekeeper",8446,
        		new ResourceConfig().registerClasses(
        				SecurityFilter.class,
        				eu.arrowhead.core.gatekeeper.GatekeeperResource.class)
        		).setSSLContext(certHandler.getSSLContext("cloud1.gatekeeper")));
        
    	serverList.add(new ServerInfo(
        		"orchestrator",8447,
        		new ResourceConfig().registerClasses(
        				SecurityFilter.class,
        				eu.arrowhead.core.orchestrator.OrchestratorResource.class)
        		).setSSLContext(certHandler.getSSLContext("cloud1.orchestrator"))); 
        
    	serverList.add(new ServerInfo(
        		"qos",8448,
        		new ResourceConfig().registerClasses(
        				SecurityFilter.class,
        				eu.arrowhead.core.qos.QoSResource.class)
        		).setSSLContext(certHandler.getSSLContext("cloud1.qos")));         
    	
    	serverList.add(new ServerInfo(
        		"api",8449,
        		new ResourceConfig().registerClasses(
        				SecurityFilter.class,
        				eu.arrowhead.core.api.ApiResource.class)
        		).setSSLContext(certHandler.getSSLContext("cloud1.qos")));            	
                
        
        /*ServerInfo testServer = new ServerInfo(
        		"myapp",8443,
        		new ResourceConfig().registerClasses(
        				SecurityFilter.class,
        				MyResource.class))
        		.setSSLContext(certHandler.getSSLContext("cloud1.client1")); */       
        
        for(ServerInfo server : serverList) {
        	server.start();
        }

        System.out.println("Press enter to exit...");
        System.in.read();
        
        for(ServerInfo server : serverList) {
        	server.stop();
        }
        
    }
}

