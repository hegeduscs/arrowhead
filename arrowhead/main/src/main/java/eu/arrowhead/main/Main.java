package eu.arrowhead.main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;


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
    	
    	CertificateHandler certHandler = new CertificateHandler("C:/Arrowhead/arrowhead/certificates")
    			.setTrustStore("mastercacerts")
    			.setDefaultPassword("123456");
    
    	List<ServerInfo> serverList = new ArrayList<ServerInfo>();
    	/*
    	serverList.add(new ServerInfo(
        		"serviceregistry",8444,
        		new ResourceConfig().registerClasses(
        				eu.arrowhead.core.serviceregistry.ServiceRegistryResource.class)
        				.packages("eu.arrowhead.common")
        		).setSSLContext(certHandler.getSSLContext("cloud1.serviceregistry")));
                
        
    	serverList.add(new ServerInfo(
        		"authorization",8445,
        		new ResourceConfig().registerClasses(
        				eu.arrowhead.core.authorization.AuthorizationResource.class)
        				.packages("eu.arrowhead.common")
        		));
        		//.setSSLContext(certHandler.getSSLContext("cloud1.authorization")));
              
        
    	serverList.add(new ServerInfo(
        		"gatekeeper",8446,
        		new ResourceConfig().registerClasses(
        				eu.arrowhead.core.gatekeeper.GatekeeperResource.class)
        				.packages("eu.arrowhead.common")
        		).setSSLContext(certHandler.getSSLContext("cloud1.gatekeeper")));
        
    	serverList.add(new ServerInfo(
        		"orchestrator",8447,
        		new ResourceConfig().registerClasses(
        				eu.arrowhead.core.orchestrator.OrchestratorResource.class)
        				.packages("eu.arrowhead.common")
        		).setSSLContext(certHandler.getSSLContext("cloud1.orchestrator"))); */
    	
    	serverList.add(new ServerInfo(
        		"orchestrator/store",8448,
        		new ResourceConfig().registerClasses(
        				eu.arrowhead.core.orchestrator.store.StoreResource.class)
        				.packages("eu.arrowhead.common")
        		));
        
    	/*serverList.add(new ServerInfo(
        		"qos",8449,
        		new ResourceConfig().registerClasses(
        				eu.arrowhead.core.qos.QoSResource.class)
        				.packages("eu.arrowhead.common")
        		).setSSLContext(certHandler.getSSLContext("cloud1.qos")));         
    	*/
    	serverList.add(new ServerInfo(
        		"api",8450,
        		new ResourceConfig().registerClasses(
        				eu.arrowhead.core.api.AuthenticationApi.class,
        				eu.arrowhead.core.api.CommonApi.class,
        				eu.arrowhead.core.api.ConfigurationApi.class,
        				eu.arrowhead.core.api.OrchestrationApi.class,
        				eu.arrowhead.core.api.ServiceRegistryApi.class)
        				.packages("eu.arrowhead.common", "eu.arrowhead.core.api.filters")
        		));
    			//.setSSLContext(certHandler.getSSLContext("cloud1.qos")));           	
                
    	
        
    	/*serverList.add(new ServerInfo(
        		"myapp",8081,
        		new ResourceConfig().registerClasses(
        				//SecurityFilter.class,
        				MyResource.class)).packages("eu.arrowhead.common")
        		.setSSLContext(certHandler.getSSLContext("cloud1.client1")));    */    
        
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

