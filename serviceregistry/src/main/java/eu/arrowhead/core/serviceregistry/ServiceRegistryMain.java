package eu.arrowhead.core.serviceregistry;

import com.github.danieln.dnssdjava.DnsSDRegistrator;
import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.Utility;
import eu.arrowhead.common.exception.AuthenticationException;
import eu.arrowhead.common.security.SecurityUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.ServiceConfigurationError;
import java.util.Timer;
import java.util.TimerTask;
import javax.net.ssl.SSLContext;
import javax.ws.rs.ProcessingException;
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
public class ServiceRegistryMain {

  public static boolean DEBUG_MODE;

  static final int pingTimeout = new Integer(getAppProp().getProperty("ping.timeout", "10000"));
  //DNS-SD global settings
  static final String tsigKeyName = getDnsProp().getProperty("tsig.name", "key.arrowhead.tmit.bme.hu");
  static final String tsigAlgorithm = getDnsProp().getProperty("tsig.algorithm", DnsSDRegistrator.TSIG_ALGORITHM_HMAC_MD5);
  static final String tsigKeyValue = getDnsProp().getProperty("tsig.key", "RM/jKKEPYB83peT0DQnYGg==");
  static final String dnsIpAddress = getDnsProp().getProperty("dns.ip", "152.66.246.237");
  static final String dnsDomain = getDnsProp().getProperty("dns.registerDomain", "srv.arrowhead.tmit.bme.hu.");
  static final String computerDomain = getDnsProp().getProperty("dns.domain", "arrowhead.tmit.bme.hu");
  static final int dnsPort = new Integer(getDnsProp().getProperty("dns.port", "53"));

  private static HttpServer server;
  private static HttpServer secureServer;
  private static Properties appProp, dnsProp;
  private static Timer timer;

  private static final String BASE_URI = getAppProp().getProperty("base_uri", "http://127.0.0.1:8442/");
  private static final String BASE_URI_SECURED = getAppProp().getProperty("base_uri_secured", "https://127.0.0.1:8443/");
  private static final Logger log = Logger.getLogger(ServiceRegistryMain.class.getName());

  /**
   * Main method.
   */
  public static void main(String[] args) throws IOException {
    PropertyConfigurator.configure("config" + File.separator + "log4j.properties");
    System.out.println("Working directory: " + System.getProperty("user.dir"));
    Utility.isUrlValid(BASE_URI, false);
    Utility.isUrlValid(BASE_URI_SECURED, true);

    //Setting up DNS
    System.setProperty("dns.server", getDnsProp().getProperty("dns.ip"));
    System.setProperty("dnssd.domain", getDnsProp().getProperty("dns.domain"));
    System.setProperty("dnssd.hostname", getDnsProp().getProperty("dns.host"));

    boolean daemon = false;
    boolean serverModeSet = false;
    for (int i = 0; i < args.length; ++i) {
      switch (args[i]) {
        case "-daemon":
          daemon = true;
          System.out.println("Starting SR bridge as daemon!");
          break;
        case "-d":
          DEBUG_MODE = true;
          System.out.println("Starting server in debug mode!");
          break;
        case "-m":
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

    //if no mode was selected in args, insecure it is
    if (!serverModeSet) {
      server = startServer();
    }

    //if scheduled ping is set
    if (getAppProp().getProperty("ping.scheduled").equals("true")) {
      TimerTask pingTask = new PingProvidersTask();
      timer = new Timer();
      int interval = 10;
      try {
        interval = Integer.parseInt(getAppProp().getProperty("ping.interval", "10"));
      } catch (Exception e) {
        log.error("Invalid 'ping.interval' value in app.properties!");
      }

      timer.schedule(pingTask, 60000L, (interval * 60L * 1000L));
    }

    //This is here to initialize the database connection before the REST resources are initiated
    DatabaseManager dm = DatabaseManager.getInstance();
    if (daemon) {
      System.out.println("In daemon mode, process will terminate for TERM signal...");
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          System.out.println("Received TERM signal, shutting down...");
          if (timer != null) {
            timer.cancel();
          }
          shutdown();
        }
      });
    } else {
      System.out.println("Type \"stop\" to shutdown Service Registry Server(s)...");
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      String input = "";
      while (!input.equals("stop")) {
        input = br.readLine();
      }
      br.close();
      if (timer != null) {
        timer.cancel();
      }
      shutdown();
    }
  }

  private static synchronized Properties getDnsProp() {
    try {
      if (dnsProp == null) {
        dnsProp = new Properties();
        File file = new File("config" + File.separator + "dns.properties");
        FileInputStream inputStream = new FileInputStream(file);
        dnsProp.load(inputStream);
      }
    } catch (FileNotFoundException ex) {
      throw new ServiceConfigurationError("App.properties file not found, make sure you have the correct working directory set! (directory where "
          + "the config folder can be found)", ex);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return dnsProp;
  }

  /**
   * @return Grizzly HTTP server.
   */
  private static HttpServer startServer() throws IOException {
    final ResourceConfig config = new ResourceConfig();
    config.registerClasses(ServiceRegistryResource.class);
    config.packages("eu.arrowhead.common", "eu.arrowhead.core.serviceregistry.filter");

    URI uri = UriBuilder.fromUri(BASE_URI).build();
    try {
      final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, config);
      server.getServerConfiguration().setAllowPayloadForUndefinedHttpMethods(true);
      server.start();
      log.info("Started server at: " + BASE_URI);
      System.out.println("Started  insecure server at: " + BASE_URI);
      //TODO register the SR service into the DNS-SD
      return server;
    } catch (ProcessingException e) {
      throw new ServiceConfigurationError(
          "Make sure you gave a valid address in the app.properties file! (Assignable to this JVM and not in use already)", e);
    }
  }

  /**
   * @return Grizzly HTTPS server.
   */
  private static HttpServer startSecureServer() throws IOException {
    final ResourceConfig config = new ResourceConfig();
    config.registerClasses(ServiceRegistryResource.class);
    config.packages("eu.arrowhead.common", "eu.arrowhead.core.serviceregistry.filter");

    String keystorePath = getAppProp().getProperty("keystore", "/home/arrowhead_test.jks");
    String keystorePass = getAppProp().getProperty("keystorepass", "arrowhead");
    String truststorePath = getAppProp().getProperty("truststore", "/home/arrowhead_test.jks");
    String truststorePass = getAppProp().getProperty("truststorepass", "arrowhead");

    SSLContextConfigurator sslCon = new SSLContextConfigurator();
    sslCon.setKeyStoreFile(keystorePath);
    sslCon.setKeyStorePass(keystorePass);
    sslCon.setTrustStoreFile(truststorePath);
    sslCon.setTrustStorePass(truststorePass);
    if (!sslCon.validateConfiguration(true)) {
      log.fatal("SSL Context is not valid, check the certificate files or app.properties!");
      throw new AuthenticationException("SSL Context is not valid, check the certificate files or app.properties!");
    }

    SSLContext sslContext = sslCon.createSSLContext();
    Utility.setSSLContext(sslContext);

    KeyStore keyStore = SecurityUtils.loadKeyStore(keystorePath, keystorePass);
    X509Certificate serverCert = SecurityUtils.getFirstCertFromKeyStore(keyStore);
    String serverCN = SecurityUtils.getCertCNFromSubject(serverCert.getSubjectDN().getName());
    if (!SecurityUtils.isKeyStoreCNArrowheadValid(serverCN)) {
      log.fatal("Server CN is not compliant with the Arrowhead cert structure");
      throw new AuthenticationException(
          "Server CN ( " + serverCN
              + ") is not compliant with the Arrowhead cert structure, since it does not have 5 parts, or does not end with arrowhead.eu.");
    }
    log.info("Certificate of the secure server: " + serverCN);
    config.property("server_common_name", serverCN);

    URI uri = UriBuilder.fromUri(BASE_URI_SECURED).build();
    try {
      final HttpServer server = GrizzlyHttpServerFactory
          .createHttpServer(uri, config, true, new SSLEngineConfigurator(sslCon).setClientMode(false).setNeedClientAuth(true));
      server.getServerConfiguration().setAllowPayloadForUndefinedHttpMethods(true);
      server.start();
      log.info("Started server at: " + BASE_URI_SECURED);
      System.out.println("Started secure server at: " + BASE_URI_SECURED);
      //TODO register the SR service into the DNS-SD
      return server;
    } catch (ProcessingException e) {
      throw new ServiceConfigurationError(
          "Make sure you gave a valid address in the app.properties file! (Assignable to this JVM and not in use already)", e);
    }
  }

  private static synchronized Properties getAppProp() {
    try {
      if (appProp == null) {
        appProp = new Properties();
        File file = new File("config" + File.separator + "app.properties");
        FileInputStream inputStream = new FileInputStream(file);
        appProp.load(inputStream);
      }
    } catch (FileNotFoundException ex) {
      throw new ServiceConfigurationError("App.properties file not found, make sure you have the correct working directory set! (directory where "
          + "the config folder can be found)", ex);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return appProp;
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
}
