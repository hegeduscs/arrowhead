package eu.arrowhead.core.serviceregistry;

import eu.arrowhead.common.Utility;
import eu.arrowhead.common.exception.AuthenticationException;
import eu.arrowhead.common.ssl.SecurityUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import javax.net.ssl.SSLContext;
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
  private static HttpServer secureServer = null;
  private static Logger log = Logger.getLogger(ServiceRegistryMain.class.getName());
  private static Properties prop;
  private static final String BASE_URI = getProp().getProperty("base_uri", "http://0.0.0.0:8442/");
  private static final String BASE_URI_SECURED = getProp().getProperty("base_uri_secured", "https://0.0.0.0:8443/");

  /**
   * Main method.
   */
  public static void main(String[] args) throws IOException {
    PropertyConfigurator.configure("config" + File.separator + "log4j.properties");

    boolean daemon = false;
    boolean serverModeSet = false;
    for (int i = 0; i < args.length; ++i) {
      if (args[i].equals("-d")) {
        daemon = true;
        System.out.println("Starting server as daemon!");
      } else if (args[i].equals("-m")) {
        serverModeSet = true;
        ++i;
        switch (args[i]) {
          case "insecure":
            server = startServer();
            break;
          case "secure":
            secureServer = startSecureServer();
            break;
          case "both":
            server = startServer();
            secureServer = startSecureServer();
            break;
          default:
            log.fatal("Unknown server mode: " + args[i]);
            throw new AssertionError("Unknown server mode: " + args[i]);
        }
      }
    }
    if (!serverModeSet) {
      server = startServer();
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

  /**
   * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
   *
   * @return Grizzly HTTP server.
   */
  private static HttpServer startServer() throws IOException {
    log.info("Starting server at: " + BASE_URI);
    System.out.println("Starting insecure server at: " + BASE_URI);

    final ResourceConfig config = new ResourceConfig();
    config.registerClasses(ServiceRegistryResource.class);
    config.packages("eu.arrowhead.common");

    URI uri = UriBuilder.fromUri(BASE_URI).build();
    final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, config);
    server.getServerConfiguration().setAllowPayloadForUndefinedHttpMethods(true);
    server.start();
    return server;
  }

  /**
   * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
   *
   * @return Grizzly HTTP server.
   */
  private static HttpServer startSecureServer() throws IOException {
    log.info("Starting server at: " + BASE_URI_SECURED);
    System.out.println("Starting secure server at: " + BASE_URI_SECURED);

    final ResourceConfig config = new ResourceConfig();
    config.registerClasses(SecureServiceRegistryResource.class);
    config.packages("eu.arrowhead.common");

    String keystorePath = getProp().getProperty("ssl.keystore", "/home/arrowhead_test.jks");
    String keystorePass = getProp().getProperty("ssl.keystorepass", "arrowhead");
    String truststorePath = getProp().getProperty("ssl.truststore", "/home/arrowhead_test.jks");
    String truststorePass = getProp().getProperty("ssl.truststorepass", "arrowhead");

    SSLContextConfigurator sslCon = new SSLContextConfigurator();
    sslCon.setKeyStoreFile(keystorePath);
    sslCon.setKeyStorePass(keystorePass);
    sslCon.setTrustStoreFile(truststorePath);
    sslCon.setTrustStorePass(truststorePass);

    SSLContext sslContext = sslCon.createSSLContext();
    Utility.setSSLContext(sslContext);

    X509Certificate serverCert;
    try {
      KeyStore keyStore = SecurityUtils.loadKeyStore(keystorePath, keystorePass);
      serverCert = SecurityUtils.getFirstCertFromKeyStore(keyStore);
    } catch (Exception ex) {
      throw new AuthenticationException(ex.getMessage());
    }
    String serverCN = SecurityUtils.getCertCNFromSubject(serverCert.getSubjectDN().getName());
    log.info("Certificate of the secure server: " + serverCN);
    config.property("server_common_name", serverCN);

    URI uri = UriBuilder.fromUri(BASE_URI_SECURED).build();
    final HttpServer server = GrizzlyHttpServerFactory
        .createHttpServer(uri, config, true, new SSLEngineConfigurator(sslCon).setClientMode(false).setNeedClientAuth(true));
    server.getServerConfiguration().setAllowPayloadForUndefinedHttpMethods(true);
    server.start();
    return server;
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

  private static synchronized Properties getProp() {
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
      log.debug("Ping and remove inactive services TimerTask at " + new Date().toString());
      ServiceRegistry.getInstance().pingAndRemoveServices();
    }
  }
}
