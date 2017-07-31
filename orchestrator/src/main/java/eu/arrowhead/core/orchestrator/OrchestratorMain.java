package eu.arrowhead.core.orchestrator;

import eu.arrowhead.common.exception.AuthenticationException;
import eu.arrowhead.common.ssl.SecurityUtils;
import eu.arrowhead.core.orchestrator.store.StoreResource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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


class OrchestratorMain {

  private static HttpServer server = null;
  private static HttpServer secureServer = null;
  private static Logger log = Logger.getLogger(OrchestratorMain.class.getName());
  private static Properties prop;
  private static final String BASE_URI = getProp()
      .getProperty("base_uri", "http://0.0.0.0:8444/orchestrator/");
  private static final String BASE_URI_SECURED = getProp()
      .getProperty("base_uri_secured", "https://0.0.0.0:8445/orchestrator/");

  public static void main(String[] args) throws IOException {
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

    if (daemon) {
      System.out.println("In daemon mode, process will terminate for TERM signal...");
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          System.out.println("Received TERM signal, shutting down...");
          shutdown();
        }
      });
    } else {
      System.out.println("Press enter to shutdown Orchestrator Server(s)...");
      System.in.read();
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
    System.out.println("Orchestrator Server(s) stopped");
  }

  private static HttpServer startServer() throws IOException {
    log.info("Starting server at: " + BASE_URI);

    URI uri = UriBuilder.fromUri(BASE_URI).build();

    final ResourceConfig config = new ResourceConfig();
    config.registerClasses(OrchestratorResource.class, StoreResource.class);
    config.packages("eu.arrowhead.common");
    config.property("isSecure", false);

    final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, config);
    server.getServerConfiguration().setAllowPayloadForUndefinedHttpMethods(true);
    server.start();
    return server;
  }

  private static HttpServer startSecureServer() throws IOException {
    log.info("Starting server at: " + BASE_URI_SECURED);

    URI uri = UriBuilder.fromUri(BASE_URI_SECURED).build();

    final ResourceConfig config = new ResourceConfig();
    config.registerClasses(OrchestratorResource.class, StoreResource.class);
    config.packages("eu.arrowhead.common");

    SSLContextConfigurator sslCon = new SSLContextConfigurator();

    String keystorePath = getProp().getProperty("ssl.keystore");
    String keystorePass = getProp().getProperty("ssl.keystorepass");
    String keyPass = getProp().getProperty("ssl.keypass");
    String truststorePath = getProp().getProperty("ssl.truststore");
    String truststorePass = getProp().getProperty("ssl.truststorepass");

    sslCon.setKeyStoreFile(keystorePath);
    sslCon.setKeyStorePass(keystorePass);
    sslCon.setKeyPass(keyPass);
    sslCon.setTrustStoreFile(truststorePath);
    sslCon.setTrustStorePass(truststorePass);

    SSLContext sslContext = sslCon.createSSLContext();
    eu.arrowhead.common.Utility.setSSLContext(sslContext);

    X509Certificate serverCert = null;
    try {
      KeyStore keyStore = SecurityUtils.loadKeyStore(keystorePath, keystorePass);
      serverCert = SecurityUtils.getFirstCertFromKeyStore(keyStore);
    } catch (Exception ex) {
      throw new AuthenticationException(ex.getMessage());
    }
    String serverCN = SecurityUtils.getCertCNFromSubject(serverCert.getSubjectDN().getName());
    log.info("Certificate of the secure server: " + serverCN);
    config.property("server_common_name", serverCN);

    final HttpServer server = GrizzlyHttpServerFactory.
        createHttpServer(uri, config, true, new SSLEngineConfigurator(sslCon)
            .setClientMode(false).setNeedClientAuth(true));
    server.getServerConfiguration().setAllowPayloadForUndefinedHttpMethods(true);
    server.start();
    return server;
  }

  private synchronized static Properties getProp() {
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
