package eu.arrowhead.main;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

class ServerInfo {
	  private String host = "localhost";
	  private int port;
	  private String name;  
	  
	  private SSLContextConfigurator sslContext;
	  private ResourceConfig rc;
	  private URI uri;
	  
	  private HttpServer server;
	  
	  public ServerInfo(String name, int port, ResourceConfig rc) {  
	    this.name = name;
	    this.port = port;
	    this.rc = rc;
	  }
	  
	  public ServerInfo setSSLContext(SSLContextConfigurator sslContext) {
	    this.sslContext = sslContext;
	    return this;
	  }
	  
	  public URI getUri() {    
	    String proto;
	    if(sslContext == null) 
	      proto = "http";    
	    else proto = "https";
	    
	    URI uri = UriBuilder.fromUri(proto+"://"+host+":"+port+"/"+name).build();
	    return uri;
	  }  
	  
	  public void start() throws IOException{
	    uri = getUri();
	    if(server==null) {
	      if(sslContext == null) {
	    	  server = GrizzlyHttpServerFactory.createHttpServer(uri, rc);
	      }
	      else {
	    	  server = GrizzlyHttpServerFactory.createHttpServer(uri, rc, true,
	    			  new SSLEngineConfigurator(sslContext).setClientMode(false).setNeedClientAuth(true)
	    	  );    
	      }   
	      System.out.println("Starting server: " + uri.toString());
	      server.start();          
	    }
	  }
	  
	  public void stop() {
	    System.out.println("Stopping server: " + uri.toString());
	    server.stop();
	  }
	    
}
