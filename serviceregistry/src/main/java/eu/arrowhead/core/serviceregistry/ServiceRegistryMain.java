package eu.arrowhead.core.serviceregistry;

import com.github.danieln.dnssdjava.DnsSDRegistrator;
import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.Utility;
import eu.arrowhead.common.exception.AuthenticationException;
import eu.arrowhead.common.security.SecurityUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
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

  private static Timer timer = null;
  //DNS-SD global settings
  static final String tsigKeyName = getDnsProp().getProperty("tsig.name", "key.arrowhead.tmit.bme.hu");
  static final String tsigAlgorithm = getDnsProp().getProperty("tsig.algorithm", DnsSDRegistrator.TSIG_ALGORITHM_HMAC_MD5);
  static final String tsigKeyValue = getDnsProp().getProperty("tsig.key", "RM/jKKEPYB83peT0DQnYGg==");
  static final String dnsIpAddress = getDnsProp().getProperty("dns.ip", "152.66.246.237");
  static final String dnsDomain = getDnsProp().getProperty("dns.registerDomain", "srv.arrowhead.tmit.bme.hu.");
  static final String computerDomain = getDnsProp().getProperty("dns.domain", "arrowhead.tmit.bme.hu");
  static final int dnsPort = new Integer(getDnsProp().getProperty("dns.port", "53"));
  //property files
  private static Properties appProp, dnsProp;
  static final int pingTimeout = new Integer(getAppProp().getProperty("ping.timeout", "10000"));
  private static final String BASE_URI = getAppProp().getProperty("base_uri", "http://0.0.0.0:8442/");
  private static final String BASE_URI_SECURED = getAppProp().getProperty("base_uri_secured", "https://0.0.0.0:8443/");
  private static HttpServer server = null;
  private static HttpServer secureServer = null;
  private static final Logger log = Logger.getLogger(ServiceRegistryMain.class.getName());

  /**
   * Main method.
   */
  public static void main(String[] args) throws IOException {

    //setting up log4j logging based on prop file
    PropertyConfigurator.configure("config" + File.separator + "log4j.properties");

    //Setting up DNS
    System.setProperty("dns.server", getDnsProp().getProperty("dns.ip"));
    System.setProperty("dnssd.domain", getDnsProp().getProperty("dns.domain"));
    System.setProperty("dnssd.hostname", getDnsProp().getProperty("dns.host"));

    boolean daemon = false;
    boolean serverModeSet = false;
    argLoop:
    for (int i = 0; i < args.length; ++i) {
      if (args[i].equals("-d")) {
        daemon = true;
        System.out.println("Starting SR bridge as daemon!");
      } else if (args[i].equals("-m")) {
        serverModeSet = true;
        ++i;
        switch (args[i]) {
          case "insecure":
            server = startServer();
            break argLoop;
          case "secure":
            secureServer = startSecureServer();
            break argLoop;
          case "both":
            server = startServer();
            secureServer = startSecureServer();
            break argLoop;
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
      System.out.println("Press enter to shutdown ServiceRegistry Server(s)...");
      //noinspection ResultOfMethodCallIgnored
      System.in.read();
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
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return dnsProp;
  }

  /**
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

    //TODO register the SR service into the DNS-SD
    return server;
  }

  /**
   * @return Grizzly HTTPS server.
   */
  private static HttpServer startSecureServer() throws IOException {
    log.info("Starting server at: " + BASE_URI_SECURED);
    System.out.println("Starting secure server at: " + BASE_URI_SECURED);

    final ResourceConfig config = new ResourceConfig();
    config.registerClasses(AccessControlFilter.class, ServiceRegistryResource.class);
    config.packages("eu.arrowhead.common");

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
    if (!SecurityUtils.isCommonNameArrowheadValid(serverCN)) {
      log.fatal("Server CN is not compliant with the Arrowhead cert structure, since it does not have 6 parts.");
      throw new AuthenticationException(
          "Server CN ( " + serverCN + ") is not compliant with the Arrowhead cert structure, since it does not have 6 parts.");
    }
    log.info("Certificate of the secure server: " + serverCN);
    config.property("server_common_name", serverCN);

    URI uri = UriBuilder.fromUri(BASE_URI_SECURED).build();
    final HttpServer server = GrizzlyHttpServerFactory
        .createHttpServer(uri, config, true, new SSLEngineConfigurator(sslCon).setClientMode(false).setNeedClientAuth(true));
    server.getServerConfiguration().setAllowPayloadForUndefinedHttpMethods(true);
    server.start();

    //TODO register the SR service into the DNS-SD
    return server;
  }

  private static synchronized Properties getAppProp() {
    try {
      if (appProp == null) {
        appProp = new Properties();
        File file = new File("config" + File.separator + "app.properties");
        FileInputStream inputStream = new FileInputStream(file);
        appProp.load(inputStream);
      }
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
