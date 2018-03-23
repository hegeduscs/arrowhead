package eu.arrowhead.core.serviceregistry;

import com.github.danieln.dnssdjava.DnsSDRegistrator;
import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.Utility;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.misc.TypeSafeProperties;
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
import java.util.Arrays;
import java.util.List;
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

public class ServiceRegistryMain {

  public static boolean DEBUG_MODE;

  static final int PING_TIMEOUT = getAppProp().getIntProperty("ping_timeout", 10000);
  //DNS-SD global settings
  static final String TSIG_NAME = getDnsProp().getProperty("tsig_name", "key.arrowhead.tmit.bme.hu");
  static final String TSIG_KEY = getDnsProp().getProperty("tsig_key", "RM/jKKEPYB83peT0DQnYGg==");
  static final String TSIG_ALGORITHM = getDnsProp().getProperty("tsig_algorithm", DnsSDRegistrator.TSIG_ALGORITHM_HMAC_MD5);
  static final String DNS_ADDRESS = getDnsProp().getProperty("dns_address", "152.66.246.237");
  static final String DNS_DOMAIN = getDnsProp().getProperty("dns_domain", "arrowhead.tmit.bme.hu");
  static final int DNS_PORT = getDnsProp().getIntProperty("dns_port", 53);
  static String DNS_REGISTRATOR_DOMAIN = getDnsProp().getProperty("dns_registrator_domain", "srv.arrowhead.tmit.bme.hu.");

  private static String BASE_URI;
  private static String BASE_URI_SECURED;
  private static HttpServer server;
  private static HttpServer secureServer;
  private static TypeSafeProperties appProp, dnsProp;
  private static Timer timer;

  private static final Logger log = Logger.getLogger(ServiceRegistryMain.class.getName());
  private static final List<String> basicPropertyNames = Arrays.asList("db_user", "db_password", "db_address");
  private static final List<String> securePropertyNames = Arrays.asList("keystore", "keystorepass", "keypass", "truststore", "truststorepass");

  public static void main(String[] args) throws IOException {
    PropertyConfigurator.configure("config" + File.separator + "log4j.properties");
    System.out.println("Working directory: " + System.getProperty("user.dir"));

    //Setting up DNS
    System.setProperty("dnssd.domain", DNS_DOMAIN);
    System.setProperty("dnssd.hostname", getDnsProp().getProperty("dns_host", "localhost"));
    if (!DNS_REGISTRATOR_DOMAIN.endsWith(".")) {
      DNS_REGISTRATOR_DOMAIN = DNS_REGISTRATOR_DOMAIN.concat(".");
    }

    String address = getAppProp().getProperty("address", "0.0.0.0");
    int insecurePort = getAppProp().getIntProperty("insecure_port", 8442);
    int securePort = getAppProp().getIntProperty("secure_port", 8443);
    BASE_URI = Utility.getUri(address, insecurePort, null, false, true);
    BASE_URI_SECURED = Utility.getUri(address, securePort, null, true, true);

    boolean daemon = false;
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
          ++i;
          switch (args[i]) {
            case "secure":
              Utility.checkProperties(getAppProp().stringPropertyNames(), basicPropertyNames, securePropertyNames, true);
              secureServer = startSecureServer();
              break;
            case "both":
              Utility.checkProperties(getAppProp().stringPropertyNames(), basicPropertyNames, securePropertyNames, true);
              server = startServer();
              secureServer = startSecureServer();
              break;
            default:
              if (!args[i].equals("insecure")) {
                System.out.println("Unknown server mode, starting insecure server!");
              }
              Utility.checkProperties(getAppProp().stringPropertyNames(), basicPropertyNames, securePropertyNames, false);
              server = startServer();
          }
      }
    }

    //if provider ping is scheduled, start the TimerTask that provides it
    if (Boolean.valueOf(getAppProp().getProperty("ping_scheduled", "false"))) {
      TimerTask pingTask = new PingProvidersTask();
      timer = new Timer();
      int interval = getAppProp().getIntProperty("ping_interval", 60);
      timer.schedule(pingTask, 60000L, (interval * 60L * 1000L));
    }

    //This is here to initialize the database connection before the REST resources are initiated
    DatabaseManager dm = DatabaseManager.getInstance();
    if (daemon) {
      System.out.println("In daemon mode, process will terminate for TERM signal...");
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        System.out.println("Received TERM signal, shutting down...");
        if (timer != null) {
          timer.cancel();
        }
        shutdown();
      }));
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
      throw new AuthException("SSL Context is not valid, check the certificate files or app.properties!");
    }

    SSLContext sslContext = sslCon.createSSLContext();
    Utility.setSSLContext(sslContext);

    KeyStore keyStore = SecurityUtils.loadKeyStore(keystorePath, keystorePass);
    X509Certificate serverCert = SecurityUtils.getFirstCertFromKeyStore(keyStore);
    String serverCN = SecurityUtils.getCertCNFromSubject(serverCert.getSubjectDN().getName());
    if (!SecurityUtils.isKeyStoreCNArrowheadValid(serverCN)) {
      log.fatal("Server CN is not compliant with the Arrowhead cert structure");
      throw new AuthException(
          "Server CN ( " + serverCN + ") is not compliant with the Arrowhead cert structure, since it does not have 5 parts, or does not "
              + "end with arrowhead.eu.");
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
    System.exit(0);
  }

  private static synchronized TypeSafeProperties getAppProp() {
    try {
      if (appProp == null) {
        appProp = new TypeSafeProperties();
        File file = new File("config" + File.separator + "app.properties");
        FileInputStream inputStream = new FileInputStream(file);
        appProp.load(inputStream);
      }
    } catch (FileNotFoundException ex) {
      throw new ServiceConfigurationError("app.properties file not found, make sure you have the correct working directory set! (directory where "
                                              + "the config folder can be found)", ex);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return appProp;
  }

  private static synchronized TypeSafeProperties getDnsProp() {
    try {
      if (dnsProp == null) {
        dnsProp = new TypeSafeProperties();
        File file = new File("config" + File.separator + "dns.properties");
        FileInputStream inputStream = new FileInputStream(file);
        dnsProp.load(inputStream);
      }
    } catch (FileNotFoundException ex) {
      throw new ServiceConfigurationError("dns.properties file not found, make sure you have the correct working directory set! (directory where "
                                              + "the config folder can be found)", ex);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return dnsProp;
  }

}
