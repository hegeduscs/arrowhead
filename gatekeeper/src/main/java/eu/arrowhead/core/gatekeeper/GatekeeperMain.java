package eu.arrowhead.core.gatekeeper;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.Utility;
import eu.arrowhead.common.exception.AuthenticationException;
import eu.arrowhead.common.security.SecurityUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Properties;
import javax.net.ssl.SSLContext;
import javax.ws.rs.core.UriBuilder;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class GatekeeperMain {

  private static HttpServer inboundServer = null;
  private static HttpServer inboundSecureServer = null;
  private static HttpServer outboundServer = null;
  private static HttpServer outboundSecureServer = null;
  private static final Logger log = Logger.getLogger(GatekeeperMain.class.getName());
  private static Properties prop;
  private static final String INBOUND_BASE_URI = getProp().getProperty("inbound_base_uri", "http://0.0.0.0:8446/");
  private static final String INBOUND_BASE_URI_SECURED = getProp().getProperty("inbound_base_uri_secured", "https://0.0.0.0:8447/");
  private static final String OUTBOUND_BASE_URI = getProp().getProperty("outbound_base_uri", "http://0.0.0.0:8448/");
  private static final String OUTBOUND_BASE_URI_SECURED = getProp().getProperty("outbound_base_uri_secured", "https://0.0.0.0:8449/");
  static final int timeout = Integer.valueOf(getProp().getProperty("timeout", "30000"));
  public static boolean DEBUG_MODE;

  public static void main(String[] args) throws IOException {
    PropertyConfigurator.configure("config" + File.separator + "log4j.properties");
    System.out.println("Working directory: " + System.getProperty("user.dir"));
    Utility.isUrlValid(INBOUND_BASE_URI, false);
    Utility.isUrlValid(INBOUND_BASE_URI_SECURED, true);
    Utility.isUrlValid(OUTBOUND_BASE_URI, false);
    Utility.isUrlValid(OUTBOUND_BASE_URI_SECURED, true);

    boolean daemon = false;
    boolean serverModeSet = false;
    argLoop:
    for (int i = 0; i < args.length; ++i) {
      switch (args[i]) {
        case "-daemon":
          daemon = true;
          System.out.println("Starting server as daemon!");
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
              inboundServer = startServer(INBOUND_BASE_URI, true);
              outboundServer = startServer(OUTBOUND_BASE_URI, false);
              break argLoop;
            case "secure":
              inboundSecureServer = startSecureServer(INBOUND_BASE_URI_SECURED, true);
              outboundSecureServer = startSecureServer(OUTBOUND_BASE_URI_SECURED, false);
              break argLoop;
            case "both":
              inboundServer = startServer(INBOUND_BASE_URI, true);
              outboundServer = startServer(OUTBOUND_BASE_URI, false);
              inboundSecureServer = startSecureServer(INBOUND_BASE_URI_SECURED, true);
              outboundSecureServer = startSecureServer(OUTBOUND_BASE_URI_SECURED, false);
              break argLoop;
            default:
              log.fatal("Unknown server mode: " + args[i]);
              throw new AssertionError("Unknown server mode: " + args[i]);
          }
      }
    }
    if (!serverModeSet) {
      inboundServer = startServer(INBOUND_BASE_URI, true);
      outboundServer = startServer(OUTBOUND_BASE_URI, false);
    }

    //This is here to initialize the database connection before the REST resources are initiated
    DatabaseManager dm = DatabaseManager.getInstance();
    if (daemon) {
      System.out.println("In daemon mode, process will terminate for TERM signal...");
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        System.out.println("Received TERM signal, shutting down...");
        shutdown();
      }));
    } else {
      System.out.println("Type \"stop\" to shutdown Gatekeeper Server(s)...");
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      String input = "";
      while (!input.equals("stop")) {
        input = br.readLine();
      }
      br.close();
      shutdown();
    }
  }

  private static HttpServer startServer(final String url, final boolean inbound) throws IOException {

    final ResourceConfig config = new ResourceConfig();
    if (inbound) {
      config.registerClasses(GatekeeperApi.class, GatekeeperInboundResource.class);
      log.info("Starting inbound server at: " + url);
      System.out.println("Starting insecure inbound server at: " + url);
    } else {
      config.registerClasses(GatekeeperOutboundResource.class);
      log.info("Starting outbound server at: " + url);
      System.out.println("Starting insecure outbound server at: " + url);
    }
    config.packages("eu.arrowhead.common", "eu.arrowhead.core.gatekeeper.filter");

    URI uri = UriBuilder.fromUri(url).build();
    final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, config);
    server.getServerConfiguration().setAllowPayloadForUndefinedHttpMethods(true);
    server.start();
    return server;
  }

  private static HttpServer startSecureServer(final String url, final boolean inbound) throws IOException {
    log.info("Starting server at: " + url);

    final ResourceConfig config = new ResourceConfig();
    if (inbound) {
      config.registerClasses(GatekeeperApi.class, GatekeeperInboundResource.class);
      log.info("Starting inbound server at: " + url);
      System.out.println("Starting secure inbound server at: " + url);
    } else {
      config.registerClasses(GatekeeperOutboundResource.class);
      log.info("Starting outbound server at: " + url);
      System.out.println("Starting secure outbound server at: " + url);
    }
    config.packages("eu.arrowhead.common", "eu.arrowhead.core.gatekeeper.filter");

    String keystorePath = getProp().getProperty("keystore");
    String keystorePass = getProp().getProperty("keystorepass");
    String keyPass = getProp().getProperty("keypass");
    String truststorePath, truststorePass;
    if (inbound) {
      truststorePath = getProp().getProperty("inbound_truststore");
      truststorePass = getProp().getProperty("inbound_truststorepass");
    } else {
      truststorePath = getProp().getProperty("outbound_truststore");
      truststorePass = getProp().getProperty("outbound_truststorepass");
    }

    SSLContextConfigurator sslCon = new SSLContextConfigurator();
    sslCon.setKeyStoreFile(keystorePath);
    sslCon.setKeyStorePass(keystorePass);
    sslCon.setKeyPass(keyPass);
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

    URI uri = UriBuilder.fromUri(url).build();
    final HttpServer server = GrizzlyHttpServerFactory
        .createHttpServer(uri, config, true, new SSLEngineConfigurator(sslCon).setClientMode(false).setNeedClientAuth(true));
    server.getServerConfiguration().setAllowPayloadForUndefinedHttpMethods(true);
    server.start();
    return server;
  }

  private static void shutdown() {
    if (inboundServer != null) {
      log.info("Stopping server at: " + INBOUND_BASE_URI);
      inboundServer.shutdownNow();
    }
    if (inboundSecureServer != null) {
      log.info("Stopping server at: " + INBOUND_BASE_URI_SECURED);
      inboundSecureServer.shutdownNow();
    }
    if (outboundServer != null) {
      log.info("Stopping server at: " + OUTBOUND_BASE_URI);
      outboundServer.shutdown();
    }
    if (outboundSecureServer != null) {
      log.info("Stopping server at: " + OUTBOUND_BASE_URI_SECURED);
      outboundSecureServer.shutdown();
    }
    System.out.println("Gatekeeper Servers stopped");
  }

  static synchronized Properties getProp() {
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

}
