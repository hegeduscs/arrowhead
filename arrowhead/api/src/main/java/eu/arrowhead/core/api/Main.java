package eu.arrowhead.core.api;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import eu.arrowhead.common.configuration.SysConfig;

public class Main {
	
	private static Logger log = Logger.getLogger(Main.class.getName());
	private static Properties prop;
	
	public static final String URI = SysConfig.getApiURI();
	public static final String secureURI = SysConfig.getApiURI().replaceFirst("http", "https");

	public static void main(String[] args) throws IOException {
		PropertyConfigurator.configure("config" + File.separator + "log4j.properties");
		
		HttpServer server = null;
		if (args != null && args[0] == "secure") {
			server = startSecureServer();
		}
		else{
			server = startServer();
		}
		
		System.out.println("Press enter to shutdown Api Server...");
        System.in.read();
        server.shutdownNow();
	}
	
	public static HttpServer startServer() throws IOException {
		log.info("Starting server: " + URI);
		return null;
	}
	
	public static HttpServer startSecureServer() throws IOException {
		log.info("Starting server: " + secureURI);
		return null;
	}

}
