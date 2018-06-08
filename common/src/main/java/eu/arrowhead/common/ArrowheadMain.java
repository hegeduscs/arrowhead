/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common;

import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.ServiceRegistryEntry;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.ExceptionType;
import eu.arrowhead.common.misc.CoreSystem;
import eu.arrowhead.common.misc.CoreSystemService;
import eu.arrowhead.common.misc.SecurityUtils;
import eu.arrowhead.common.misc.TypeSafeProperties;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.Set;
import javax.net.ssl.SSLContext;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public abstract class ArrowheadMain {

  public static final List<String> dbFields = Collections.unmodifiableList(Arrays.asList("db_user", "db_password", "db_address"));
  public static final List<String> certFields = Collections
      .unmodifiableList(Arrays.asList("keystore", "keystorepass", "keypass", "truststore", "truststorepass"));
  public static final Map<String, String> secureServerMetadata = Collections.singletonMap("security", "certificate");

  protected String srBaseUri;
  protected final TypeSafeProperties props = Utility.getProp("app.properties");

  private boolean daemon = false;
  private CoreSystem coreSystem;
  private HttpServer server;
  private String baseUri;
  private String base64PublicKey;

  private static final Logger log = Logger.getLogger(ArrowheadMain.class.getName());

  static {
    PropertyConfigurator.configure("config" + File.separator + "log4j.properties");
  }

  protected void init(CoreSystem coreSystem, String[] args, Set<Class<?>> classes, String[] packages) {
    System.out.println("Working directory: " + System.getProperty("user.dir"));
    this.coreSystem = coreSystem;
    DatabaseManager.init();

    boolean isSecure = false;
    for (String arg : args) {
      switch (arg) {
        case "-daemon":
          daemon = true;
          System.out.println("Starting server as daemon!");
          break;
        case "-d":
          System.setProperty("debug_mode", "true");
          System.out.println("Starting server in debug mode!");
          break;
        case "-tls":
          isSecure = true;
          break;
      }
    }

    String address = props.getProperty("address", "0.0.0.0");
    int port = isSecure ? props.getIntProperty("secure_port", coreSystem.getSecurePort())
                        : props.getIntProperty("insecure_port", coreSystem.getInsecurePort());
    baseUri = Utility.getUri(address, port, null, isSecure, true);

    if (isSecure) {
      List<String> allMandatoryProperties = new ArrayList<>(coreSystem.getAlwaysMandatoryFields());
      allMandatoryProperties.addAll(coreSystem.getSecureMandatoryFields());
      Utility.checkProperties(props.stringPropertyNames(), allMandatoryProperties);
      startSecureServer(classes, packages);
    } else {
      Utility.checkProperties(props.stringPropertyNames(), coreSystem.getAlwaysMandatoryFields());
      startServer(classes, packages);
    }

    if (!coreSystem.equals(CoreSystem.SERVICE_REGISTRY_DNS) && !coreSystem.equals(CoreSystem.SERVICE_REGISTRY_SQL)) {
      String srAddress = props.getProperty("sr_address", "0.0.0.0");
      int srPort = isSecure ? props.getIntProperty("sr_secure_port", CoreSystem.SERVICE_REGISTRY_SQL.getSecurePort())
                            : props.getIntProperty("sr_insecure_port", CoreSystem.SERVICE_REGISTRY_SQL.getInsecurePort());
      srBaseUri = Utility.getUri(srAddress, srPort, "serviceregistry", isSecure, true);
      Utility.setServiceRegistryUri(srBaseUri);
      useSRService(true);
    }
  }

  protected void listenForInput() {
    if (daemon) {
      System.out.println("In daemon mode, process will terminate for TERM signal...");
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        System.out.println("Received TERM signal, shutting down...");
        shutdown();
      }));
    } else {
      System.out.println("Type \"stop\" to shutdown " + coreSystem.name() + " Server...");
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      String input = "";
      try {
        while (!input.equals("stop")) {
          input = br.readLine();
        }
        br.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      shutdown();
    }
  }

  private void startServer(Set<Class<?>> classes, String[] packages) {
    final ResourceConfig config = new ResourceConfig();
    config.registerClasses(classes);
    config.packages(packages);

    URI uri = UriBuilder.fromUri(baseUri).build();
    try {
      server = GrizzlyHttpServerFactory.createHttpServer(uri, config, false);
      server.getServerConfiguration().setAllowPayloadForUndefinedHttpMethods(true);
      server.start();
      log.info("Started server at: " + baseUri);
      System.out.println("Started insecure server at: " + baseUri);
    } catch (IOException | ProcessingException e) {
      throw new ServiceConfigurationError(
          "Make sure you gave a valid address in the app.properties file! (Assignable to this JVM and not in use already)", e);
    }
  }

  protected void startSecureServer(Set<Class<?>> classes, String[] packages) {
    final ResourceConfig config = new ResourceConfig();
    config.registerClasses(classes);
    config.packages(packages);

    String keystorePath = props.getProperty("keystore");
    String keystorePass = props.getProperty("keystorepass");
    String keyPass = props.getProperty("keypass");
    String truststorePath = props.getProperty("truststore");
    String truststorePass = props.getProperty("truststorepass");

    SSLContextConfigurator sslCon = new SSLContextConfigurator();
    sslCon.setKeyStoreFile(keystorePath);
    sslCon.setKeyStorePass(keystorePass);
    sslCon.setKeyPass(keyPass);
    sslCon.setTrustStoreFile(truststorePath);
    sslCon.setTrustStorePass(truststorePass);
    if (!sslCon.validateConfiguration(true)) {
      log.fatal("SSL Context is not valid, check the certificate files or app.properties!");
      throw new AuthException("SSL Context is not valid, check the certificate files or app.properties!", Status.UNAUTHORIZED.getStatusCode());
    }

    SSLContext sslContext = sslCon.createSSLContext();
    Utility.setSSLContext(sslContext);

    KeyStore keyStore = SecurityUtils.loadKeyStore(keystorePath, keystorePass);
    X509Certificate serverCert = SecurityUtils.getFirstCertFromKeyStore(keyStore);
    base64PublicKey = Base64.getEncoder().encodeToString(serverCert.getPublicKey().getEncoded());
    System.out.println("Server PublicKey Base64: " + base64PublicKey);
    String serverCN = SecurityUtils.getCertCNFromSubject(serverCert.getSubjectDN().getName());
    if (!SecurityUtils.isKeyStoreCNArrowheadValid(serverCN)) {
      log.fatal("Server CN is not compliant with the Arrowhead cert structure");
      throw new AuthException(
          "Server CN ( " + serverCN + ") is not compliant with the Arrowhead cert structure, since it does not have 5 parts, or does not end with"
              + " \"arrowhead.eu.\"", Status.UNAUTHORIZED.getStatusCode());
    }
    log.info("Certificate of the secure server: " + serverCN);
    config.property("server_common_name", serverCN);

    URI uri = UriBuilder.fromUri(baseUri).build();
    try {
      server = GrizzlyHttpServerFactory
          .createHttpServer(uri, config, true, new SSLEngineConfigurator(sslCon).setClientMode(false).setNeedClientAuth(true), false);
      server.getServerConfiguration().setAllowPayloadForUndefinedHttpMethods(true);
      server.start();
      log.info("Started server at: " + baseUri);
      System.out.println("Started secure server at: " + baseUri);
    } catch (IOException | ProcessingException e) {
      throw new ServiceConfigurationError(
          "Make sure you gave a valid address in the app.properties file! (Assignable to this JVM and not in use already)", e);
    }
  }

  private void shutdown() {
    useSRService(false);
    DatabaseManager.closeSessionFactory();
    if (server != null) {
      log.info("Stopping server at: " + baseUri);
      server.shutdownNow();
    }
    System.out.println(coreSystem + " Server stopped");
    System.exit(0);
  }

  private void useSRService(boolean registering) {
    //Preparing the payload
    URI uri = UriBuilder.fromUri(baseUri).build();
    boolean isSecure = uri.getScheme().equals("https");
    ArrowheadSystem provider = new ArrowheadSystem(coreSystem.name(), uri.getHost(), uri.getPort(), base64PublicKey);

    for (CoreSystemService service : coreSystem.getServices()) {
      ArrowheadService providedService = new ArrowheadService(Utility.createSD(service.getServiceDef(), isSecure), Collections.singletonList("JSON"),
                                                              null);
      if (isSecure) {
        providedService.setServiceMetadata(ArrowheadMain.secureServerMetadata);
      }
      ServiceRegistryEntry srEntry = new ServiceRegistryEntry(providedService, provider, service.getServiceUri());

      if (registering) {
        try {
          Utility.sendRequest(UriBuilder.fromUri(srBaseUri).path("register").build().toString(), "POST", srEntry);
        } catch (ArrowheadException e) {
          if (e.getExceptionType() == ExceptionType.DUPLICATE_ENTRY) {
            Utility.sendRequest(UriBuilder.fromUri(srBaseUri).path("remove").build().toString(), "PUT", srEntry);
            Utility.sendRequest(UriBuilder.fromUri(srBaseUri).path("register").build().toString(), "POST", srEntry);
          } else {
            throw new ArrowheadException(service.getServiceDef() + " service registration failed.", e);
          }
        }
      } else {
        Utility.sendRequest(UriBuilder.fromUri(srBaseUri).path("remove").build().toString(), "PUT", srEntry);
      }
    }
  }

}
