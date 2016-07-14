package eu.arrowhead.core.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import javax.ws.rs.core.UriBuilder;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;


public class Main {
	
	private static Logger log = Logger.getLogger(Main.class.getName());
	private static Properties prop;
	
	public static final String BASE_URI = getProp().getProperty("base_uri", "http://0.0.0.0:8450/api/");
	public static final String BASE_URI_SECURED = getProp().getProperty("base_uri_secured", "https://0.0.0.0:8451/api/");

	public static void main(String[] args) throws IOException {
		PropertyConfigurator.configure("config" + File.separator + "log4j.properties");
		
		HttpServer server = null;
		HttpServer secureServer = null;
		if (args != null && args.length > 0) {
			switch (args[0]) {
			case "secure":
				secureServer = startSecureServer();
				break;
			case "both":
				server = startServer();
				secureServer = startSecureServer();
			}
		}
		else{
			server = startServer();
		}
		
		System.out.println("Press enter to shutdown Api Server(s)...");
        System.in.read();
        
        if(server != null){
        	log.info("Stopping server at: " + BASE_URI);
        	server.shutdownNow();
        }
        if(secureServer != null){
        	log.info("Stopping server at: " + BASE_URI);
        	secureServer.shutdownNow();
        }
        
        System.out.println("Api Server(s) stopped");
	}
	
	public static HttpServer startServer() throws IOException {
		log.info("Starting server at: " + BASE_URI);
		
		URI uri = UriBuilder.fromUri(BASE_URI).build();

		final ResourceConfig config = new ResourceConfig();
		config.registerClasses(AuthorizationApi.class, 
								CommonApi.class, 
								ConfigurationApi.class,
								OrchestratorApi.class);
		config.packages("eu.arrowhead.common");

		final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, config);
		server.getServerConfiguration().setAllowPayloadForUndefinedHttpMethods(true);		
		server.start();		
		return server;
	}
	
	public static HttpServer startSecureServer() throws IOException {
		log.info("Starting server at: " + BASE_URI_SECURED);
		
		URI uri = UriBuilder.fromUri(BASE_URI_SECURED).build();
		
		final ResourceConfig config = new ResourceConfig();
		config.registerClasses(AuthorizationApi.class, 
								CommonApi.class, 
								ConfigurationApi.class,
								OrchestratorApi.class);
		config.packages("eu.arrowhead.common");

		SSLContextConfigurator sslCon = new SSLContextConfigurator();

		//TODO review app.properties and default values here too
		String keystorePath = getProp().getProperty("ssl.keystore", "/home/arrowhead_test.jks");
		String keystorePass = getProp().getProperty("ssl.keystorepass", "arrowhead");
		String truststorePath = getProp().getProperty("ssl.truststore", "/home/arrowhead_test.jks");
		String truststorePass = getProp().getProperty("ssl.truststorepass", "arrowhead");
		
		sslCon.setKeyStoreFile(keystorePath);
		sslCon.setKeyStorePass(keystorePass);
		sslCon.setTrustStoreFile(truststorePath);
		sslCon.setTrustStorePass(truststorePass);

		final HttpServer server = GrizzlyHttpServerFactory.
				createHttpServer(uri, config, true, new SSLEngineConfigurator(sslCon)
				.setClientMode(false).setNeedClientAuth(true));
		server.getServerConfiguration().setAllowPayloadForUndefinedHttpMethods(true);
		server.start();
		return server;
	}
	
	public synchronized static Properties getProp() {
		try {
			if (prop == null) {
				prop = new Properties();
				File file = new File("config" + File.separator + "app.properties");
				FileInputStream inputStream = new FileInputStream(file);
				if (inputStream != null) {
					prop.load(inputStream);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return prop;
	}
}
