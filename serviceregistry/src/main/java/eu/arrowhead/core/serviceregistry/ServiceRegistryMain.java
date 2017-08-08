package eu.arrowhead.core.serviceregistry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import javax.ws.rs.core.UriBuilder;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Main class.
 */
class ServiceRegistryMain {

  private static HttpServer server = null;
  // Base URI the Grizzly HTTP server will listen on
  private static HttpServer secureServer = null;
  private static Logger log = Logger.getLogger(ServiceRegistryMain.class.getName());
  private static Properties prop;
  private static final String BASE_URI = getProp().getProperty("base_uri", "http://0.0.0.0:8080/core/");
  private static final String BASE_URI_SECURED = getProp().getProperty("base_uri_secured", "https://0.0.0.0:8443/core/");

  /**
   * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
   *
   * @return Grizzly HTTP server.
   */
  private static HttpServer startSecureServer() throws IOException {
    // create a resource config that scans for JAX-RS resources and
    // providers

    final ResourceConfig config = new ResourceConfig();
    config.registerClasses(SecureServiceRegistryResource.class);
    config.packages("eu.arrowhead.common");

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

    final HttpServer server = GrizzlyHttpServerFactory
        .createHttpServer(uri, config, true, new SSLEngineConfigurator(sslCon).setClientMode(false).setNeedClientAuth(true));
    server.getServerConfiguration().setAllowPayloadForUndefinedHttpMethods(true);
    server.start();
    return server;
  }

  /**
   * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
   *
   * @return Grizzly HTTP server.
   */
  private static HttpServer startServer() throws IOException {
    // create a resource config that scans for JAX-RS resources and
    // providers

    final ResourceConfig config = new ResourceConfig();
    config.registerClasses(ServiceRegistryResource.class);
    config.packages("eu.arrowhead.common");

    URI uri = UriBuilder.fromUri(BASE_URI).build();
    final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, config);
    log.debug("isAllowPayloadForUndefinedHttpMethods : " + server.getServerConfiguration().isAllowPayloadForUndefinedHttpMethods());
    server.getServerConfiguration().setAllowPayloadForUndefinedHttpMethods(true);
    log.debug("isAllowPayloadForUndefinedHttpMethods : " + server.getServerConfiguration().isAllowPayloadForUndefinedHttpMethods());
    server.start();
    return server;
  }

  /**
   * Main method.
   */
  public static void main(String[] args) throws IOException {
    log.info("Starting Server!");
    PropertyConfigurator.configure("config" + File.separator + "log4j.properties");

    boolean daemon = false;
    int mode = 0;

    for (int i = 0; i < args.length; ++i) {
      if (args[i].equals("-d")) {
        daemon = true;
        System.out.println("Starting server as daemon!");
      } else if (args[i].equals("-m")) {
        ++i;
        switch (args[i]) {
          case "secure":
            mode = 1;
            break;
          case "both":
            mode = 2;
            break;
          default:
            log.error("Unkown mode: " + args[i]);
        }

      }
    }

    switch (mode) {
      case 0:
        server = startServer();
        break;
      case 1:
        System.out.println("Starting secure server...");
        secureServer = startSecureServer();
        break;
      case 2:
        System.out.println("Starting secure and unsecure servers...");
        server = startServer();
        secureServer = startSecureServer();
        break;
    }

    TimerTask pingTask = new PingTask();
    final Timer timer = new Timer();
    int interval = 10;
    try {
      interval = Integer.parseInt(getProp().getProperty("ping.interval", "10"));
    } catch (Exception e) {
      log.error("Invalid 'ping.interval' value in app.properties!");
    }

    timer.schedule(pingTask, 60000L, (interval * 60L * 1000L));

    if (daemon) {
      System.out.println("In daemon mode, process will terminate for TERM signal...");
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          System.out.println("Received TERM signal, shutting down...");
          timer.cancel();
          shutdown();
        }
      });
    } else {
      System.out.println("Press enter to shutdown ServiceRegistry Server(s)...");
      System.in.read();
      timer.cancel();
      shutdown();
    }

  }

  private static void shutdown() {
    if (server != null) {
      log.info("Stopping server at: " + BASE_URI);
      server.shutdownNow();
    }
    if (secureServer != null) {
      log.info("Stopping server at: " + BASE_URI_SECURED);
      secureServer.shutdownNow();
    }
    System.out.println("Service Registry Server(s) stopped");
  }

  private synchronized static Properties getProp() {
    try {
      if (prop == null) {
        prop = new Properties();
        File file = new File("config" + File.separator + "app.properties");
        FileInputStream inputStream = new FileInputStream(file);
        prop.load(inputStream);
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
