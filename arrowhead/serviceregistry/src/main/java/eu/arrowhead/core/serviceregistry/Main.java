package eu.arrowhead.core.serviceregistry;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import eu.arrowhead.common.filter.EmptyPayloadFilter;
import eu.arrowhead.common.filter.LoggingRequestFilter;
import eu.arrowhead.common.filter.LoggingResponseFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.ws.rs.core.UriBuilder;

/**
 * Main class.
 *
 */
public class Main {

	private static Logger log = Logger.getLogger(Main.class.getName());
	// Base URI the Grizzly HTTP server will listen on

	public static final String BASE_URI = getProp().getProperty("base_uri", "http://0.0.0.0:8080/core/");
	public static final String BASE_URI_SECURED = getProp().getProperty("base_uri_secured", "https://0.0.0.0:8443/core/");

	private static Properties prop;

	/**
	 * Starts Grizzly HTTP server exposing JAX-RS resources defined in this
	 * application.
	 * 
	 * @return Grizzly HTTP server.
	 * @throws IOException
	 */
	public static HttpServer startSecureServer() throws IOException {
		// create a resource config that scans for JAX-RS resources and
		// providers

		final ResourceConfig config = new ResourceConfig();
		config.registerClasses(SecureServiceRegistryResource.class, SecurityFilter.class, EmptyPayloadFilter.class,
				LoggingRequestFilter.class, LoggingResponseFilter.class);

		URI uri = UriBuilder.fromUri(BASE_URI_SECURED).build();

		SSLContextConfigurator sslCon = new SSLContextConfigurator();

		String keystorePath = getProp().getProperty("ssl.keystore", "/home/arrowhead_test.jks");
		String keystorePass = getProp().getProperty("ssl.keystorepass", "arrowhead");
		String truststorePath = getProp().getProperty("ssl.truststore", "/home/arrowhead_test.jks");
		String truststorePass = getProp().getProperty("ssl.truststorepass", "arrowhead");
		
		sslCon.setKeyStoreFile(keystorePath);
		sslCon.setKeyStorePass(keystorePass);
		sslCon.setTrustStoreFile(truststorePath);
		sslCon.setTrustStorePass(truststorePass);

		final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, config, true, new SSLEngineConfigurator(sslCon)
				.setClientMode(false).setNeedClientAuth(true));

		server.start();
		return server;
	}

	/**
	 * Starts Grizzly HTTP server exposing JAX-RS resources defined in this
	 * application.
	 * 
	 * @return Grizzly HTTP server.
	 * @throws IOException
	 */
	public static HttpServer startServer() throws IOException {
		// create a resource config that scans for JAX-RS resources and
		// providers

		final ResourceConfig config = new ResourceConfig();
		config.registerClasses(ServiceRegistryResource.class, EmptyPayloadFilter.class, LoggingRequestFilter.class,
				LoggingResponseFilter.class);

		URI uri = UriBuilder.fromUri(BASE_URI).build();
		final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, config);

		server.start();
		return server;
	}

	/**
	 * Main method.
	 * 
	 * @param args
	 * @throws IOException
	 */
	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws IOException {
		log.info("Starting Server!");
		PropertyConfigurator.configure("config" + File.separator + "log4j.properties");

		HttpServer server = null;
		if (args != null && args.length > 0) {
			switch (args[0]) {
			case "insec":
				server = startServer();
				log.debug(String.format("Jersey app started with WADL available at "
						+ "%sapplication.wadl", BASE_URI));
				break;
			default:
				break;
			}
		} else {
			server = startSecureServer();
			log.debug(String.format("Jersey app started with WADL available at " + "%sapplication.wadl",
					BASE_URI_SECURED));
		}

		TimerTask pingTask = new PingTask();
		Timer timer = new Timer();
		timer.schedule(pingTask, 60000l, (2l * 60l * 1000l));

		System.in.read();
		server.stop();
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

	static class PingTask extends TimerTask {

		@Override
		public void run() {
			log.debug("TimerTask " + new Date().toString());
			ServiceRegistry.getInstance().pingAndRemoveServices();
		}
	}
}
