package eu.arrowhead.core.serviceregistry;

import org.apache.log4j.PropertyConfigurator;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

import javax.ws.rs.core.UriBuilder;

/**
 * Main class.
 *
 */
public class Main {
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
	public static HttpServer startServer() throws IOException {
		// create a resource config that scans for JAX-RS resources and
		// providers
		// in com.example package
		final ResourceConfig rc = new ResourceConfig().registerClasses(ServiceRegistryResource.class, SecurityFilter.class);

		URI uri = UriBuilder.fromUri(BASE_URI).build();
		URI uri_sec = UriBuilder.fromUri(BASE_URI_SECURED).build();

		SSLContextConfigurator sslCon = new SSLContextConfigurator();

		String keystorePath = getProp().getProperty("ssl.keystore", "/home/arrowhead_test.jks");
		String keystorePass = getProp().getProperty("ssl.keystorepass", "arrowhead");
		String truststorePath = getProp().getProperty("ssl.truststore", "/home/arrowhead_test.jks");
		String truststorePass = getProp().getProperty("ssl.truststorepass", "arrowhead");
		System.out.println(" keystorePath " + keystorePath);
		sslCon.setKeyStoreFile(keystorePath);
		sslCon.setKeyStorePass(keystorePass);
		sslCon.setTrustStoreFile(truststorePath);
		sslCon.setTrustStorePass(truststorePass);

		// create and start a new instance of grizzly http server
		// exposing the Jersey application at BASE_URI
		// HttpServer server =
		// GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
		final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri_sec, rc, true, new SSLEngineConfigurator(sslCon)
				.setClientMode(false).setNeedClientAuth(true));

		server.start();

		return server;
	}

	/**
	 * Main method.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		PropertyConfigurator.configure("config" + File.separator + "log4j.properties");

		final HttpServer server = startServer();
		System.out.println(String.format("Jersey app started with WADL available at "
				+ "%sapplication.wadl\nHit enter to stop it...", BASE_URI_SECURED));
		System.in.read();
		server.stop();
	}

	public synchronized static Properties getProp() {
		try {
			if (prop == null) {
				prop = new Properties();
				File file = new File("config" + File.separator + "app.properties");
				FileInputStream inputStream = new FileInputStream(file);
				// InputStream inputStream =
				// Main.class.getClassLoader().getResourceAsStream("app.properties");
				if (inputStream != null) {
					prop.load(inputStream);
				} /*
				 * else { throw new FileNotFoundException(
				 * "property file 'app.properties' not found in the classpath");
				 * }
				 */
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return prop;
	}
}
