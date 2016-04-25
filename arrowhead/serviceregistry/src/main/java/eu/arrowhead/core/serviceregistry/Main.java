package eu.arrowhead.core.serviceregistry;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

/**
 * Main class.
 *
 */
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://0.0.0.0:8080/core/";
    public static final String BASE_URI_SECURED = "https://0.0.0.0:8443/core/";

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     * @throws IOException 
     */
    public static HttpServer startServer() throws IOException {
    	// create a resource config that scans for JAX-RS resources and providers
        // in com.example package
        final ResourceConfig rc = new ResourceConfig().registerClasses(ServiceRegistryResource.class,SecurityFilter.class);
        
        URI uri = UriBuilder.fromUri(BASE_URI).build();
        URI uri_sec = UriBuilder.fromUri(BASE_URI_SECURED).build();
        
        SSLContextConfigurator sslCon = new SSLContextConfigurator();
        sslCon.setKeyStoreFile("/home/arrowhead_test.jks");
        sslCon.setKeyStorePass("arrowhead");
        sslCon.setTrustStoreFile("/home/arrowhead_test.jks");
        sslCon.setTrustStorePass("arrowhead");
        
        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        //HttpServer server =  GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
        		uri_sec,
        		rc,
        		true,
        		new SSLEngineConfigurator(sslCon).setClientMode(false).setNeedClientAuth(true)
        );       
        
        server.start();
        
        return server;
    }

    /**
     * Main method.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...", BASE_URI_SECURED));
        System.in.read();
        server.stop();
    }
}

