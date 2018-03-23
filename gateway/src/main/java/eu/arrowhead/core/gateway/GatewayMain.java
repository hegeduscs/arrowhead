/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.gateway;

import eu.arrowhead.common.Utility;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.ServiceRegistryEntry;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.ExceptionType;
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
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.ServiceConfigurationError;
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

public class GatewayMain {

  public static boolean DEBUG_MODE;
  public static SSLContext clientContext;

  private static String BASE_URI;
  private static String BASE_URI_SECURED;
  private static String SR_BASE_URI;
  private static String SR_BASE_URI_SECURED;
  private static String BASE64_PUBLIC_KEY;
  private static HttpServer server;
  private static HttpServer secureServer;
  private static TypeSafeProperties prop;

  private static final Logger log = Logger.getLogger(GatewayMain.class.getName());

  public static void main(String[] args) throws IOException {
    PropertyConfigurator.configure("config" + File.separator + "log4j.properties");
    System.out.println("Working directory: " + System.getProperty("user.dir"));

    String address = getProp().getProperty("address", "0.0.0.0");
    int insecurePort = getProp().getIntProperty("insecure_port", 8452);
    int securePort = getProp().getIntProperty("secure_port", 8453);

    String sr_address = getProp().getProperty("sr_address", "0.0.0.0");
    int srInsecurePort = getProp().getIntProperty("sr_insecure_port", 8442);
    int srSecurePort = getProp().getIntProperty("secure_port", 8443);

    boolean daemon = false;
    for (String arg : args) {
      switch (arg) {
        case "-daemon":
          daemon = true;
          System.out.println("Starting server as daemon!");
          break;
        case "-d":
          DEBUG_MODE = true;
          System.out.println("Starting server in debug mode!");
          break;
        case "-tls":
          List<String> secureMandatoryProperties = Arrays
              .asList("keystore", "keystorepass", "keypass", "truststore", "truststorepass", "trustpass", "master_arrowhead_cert");
          Utility.checkProperties(getProp().stringPropertyNames(), secureMandatoryProperties);
          BASE_URI_SECURED = Utility.getUri(address, securePort, null, true, true);
          SR_BASE_URI_SECURED = Utility.getUri(sr_address, srSecurePort, "serviceregistry", true, true);
          secureServer = startSecureServer();
          useSRService(true, true);
      }
    }
    if (secureServer == null) {
      BASE_URI = Utility.getUri(address, insecurePort, null, false, true);
      SR_BASE_URI = Utility.getUri(sr_address, srInsecurePort, "serviceregistry", false, true);
      server = startServer();
      useSRService(false, true);
    }

    if (daemon) {
      System.out.println("In daemon mode, process will terminate for TERM signal...");
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        System.out.println("Received TERM signal, shutting down...");
        shutdown();
      }));
    } else {
      System.out.println("Type \"stop\" to shutdown Gateway Server(s)...");
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      String input = "";
      while (!input.equals("stop")) {
        input = br.readLine();
      }
      br.close();
      shutdown();
    }
  }

  private static HttpServer startServer() throws IOException {
    final ResourceConfig config = new ResourceConfig();
    config.registerClasses(GatewayApi.class, GatewayResource.class);
    config.packages("eu.arrowhead.common", "eu.arrowhead.core.gateway.filter");

    URI uri = UriBuilder.fromUri(BASE_URI).build();
    try {
      final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, config);
      server.getServerConfiguration().setAllowPayloadForUndefinedHttpMethods(true);
      server.start();
      log.info("Started server at: " + BASE_URI);
      System.out.println("Started insecure server at: " + BASE_URI);
      return server;
    } catch (ProcessingException e) {
      throw new ServiceConfigurationError(
          "Make sure you gave a valid address in the app.properties file! (Assignable to this JVM and not in use already)", e);
    }
  }

  private static HttpServer startSecureServer() throws IOException {
    final ResourceConfig config = new ResourceConfig();
    config.registerClasses(GatewayApi.class, GatewayResource.class);
    config.packages("eu.arrowhead.common", "eu.arrowhead.core.gateway.filter");

    String keystorePath = getProp().getProperty("keystore");
    String keystorePass = getProp().getProperty("keystorepass");
    String keyPass = getProp().getProperty("keypass");
    String truststorePath = getProp().getProperty("truststore");
    String truststorePass = getProp().getProperty("truststorepass");
    String trustPass = getProp().getProperty("trustpass");
    String masterArrowheadCertPath = getProp().getProperty("master_arrowhead_cert");

    SSLContextConfigurator sslCon = new SSLContextConfigurator();
    sslCon.setKeyStoreFile(keystorePath);
    sslCon.setKeyStorePass(keystorePass);
    sslCon.setKeyPass(keyPass);
    sslCon.setTrustStoreFile(truststorePath);
    sslCon.setTrustStorePass(truststorePass);
    if (!sslCon.validateConfiguration(true)) {
      log.fatal("SSL Context is not valid, check the certificate files or app.properties!");
      throw new AuthException("SSL Context is not valid, check the certificate files or app.properties!");
    }

    Utility.setSSLContext(sslCon.createSSLContext());
    clientContext = SecurityUtils.createMasterSSLContext(truststorePath, truststorePass, trustPass, masterArrowheadCertPath);

    KeyStore keyStore = SecurityUtils.loadKeyStore(keystorePath, keystorePass);
    X509Certificate serverCert = SecurityUtils.getFirstCertFromKeyStore(keyStore);
    BASE64_PUBLIC_KEY = Base64.getEncoder().encodeToString(serverCert.getPublicKey().getEncoded());
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
      return server;
    } catch (ProcessingException e) {
      throw new ServiceConfigurationError(
          "Make sure you gave a valid address in the app.properties file! (Assignable to this JVM and not in use already)", e);
    }
  }

  private static void useSRService(boolean isSecure, boolean registering) {
    String SRU = isSecure ? SR_BASE_URI_SECURED : SR_BASE_URI;
    URI uri = isSecure ? UriBuilder.fromUri(BASE_URI_SECURED).build() : UriBuilder.fromUri(BASE_URI).build();
    ArrowheadSystem gatewaySystem = new ArrowheadSystem("gateway", uri.getHost(), uri.getPort(), BASE64_PUBLIC_KEY);
    ArrowheadService providerService = new ArrowheadService(Utility.createSD(Utility.GW_PROVIDER_SERVICE, isSecure),
                                                            Collections.singletonList("JSON"), null);
    ArrowheadService consumerService = new ArrowheadService(Utility.createSD(Utility.GW_CONSUMER_SERVICE, isSecure),
                                                            Collections.singletonList("JSON"), null);
    ArrowheadService mgmtService = new ArrowheadService(Utility.createSD(Utility.GW_SESSION_MGMT, isSecure), Collections.singletonList("JSON"), null);
    if (isSecure) {
      providerService.setServiceMetadata(Utility.secureServerMetadata);
      consumerService.setServiceMetadata(Utility.secureServerMetadata);
      mgmtService.setServiceMetadata(Utility.secureServerMetadata);
    }

    //Preparing the payloads
    ServiceRegistryEntry providerEntry = new ServiceRegistryEntry(providerService, gatewaySystem, "gateway/connectToProvider");
    ServiceRegistryEntry consumerEntry = new ServiceRegistryEntry(consumerService, gatewaySystem, "gateway/connectToConsumer");
    ServiceRegistryEntry mgmtEntry = new ServiceRegistryEntry(mgmtService, gatewaySystem, "gateway/management");

    if (registering) {
      try {
        Utility.sendRequest(UriBuilder.fromUri(SRU).path("register").build().toString(), "POST", providerEntry);
      } catch (ArrowheadException e) {
        if (e.getExceptionType() == ExceptionType.DUPLICATE_ENTRY) {
          Utility.sendRequest(UriBuilder.fromUri(SRU).path("remove").build().toString(), "PUT", providerEntry);
          Utility.sendRequest(UriBuilder.fromUri(SRU).path("register").build().toString(), "POST", providerEntry);
        } else {
          throw new ArrowheadException("Gateway CTP service registration failed.", e);
        }
      }
      try {
        Utility.sendRequest(UriBuilder.fromUri(SRU).path("register").build().toString(), "POST", consumerEntry);
      } catch (ArrowheadException e) {
        if (e.getExceptionType() == ExceptionType.DUPLICATE_ENTRY) {
          Utility.sendRequest(UriBuilder.fromUri(SRU).path("remove").build().toString(), "PUT", consumerEntry);
          Utility.sendRequest(UriBuilder.fromUri(SRU).path("register").build().toString(), "POST", consumerEntry);
        } else {
          throw new ArrowheadException("Gateway CTC service registration failed.", e);
        }
      }
      try {
        Utility.sendRequest(UriBuilder.fromUri(SRU).path("register").build().toString(), "POST", mgmtEntry);
      } catch (ArrowheadException e) {
        if (e.getExceptionType() == ExceptionType.DUPLICATE_ENTRY) {
          Utility.sendRequest(UriBuilder.fromUri(SRU).path("remove").build().toString(), "PUT", mgmtEntry);
          Utility.sendRequest(UriBuilder.fromUri(SRU).path("register").build().toString(), "POST", mgmtEntry);
        } else {
          throw new ArrowheadException("Gateway management service registration failed.", e);
        }
      }
    } else {
      Utility.sendRequest(UriBuilder.fromUri(SRU).path("remove").build().toString(), "PUT", providerEntry);
      Utility.sendRequest(UriBuilder.fromUri(SRU).path("remove").build().toString(), "PUT", consumerEntry);
      Utility.sendRequest(UriBuilder.fromUri(SRU).path("remove").build().toString(), "PUT", mgmtEntry);
      System.out.println("Gateway services deregistered.");
    }
  }

  private static void shutdown() {
    if (server != null) {
      log.info("Stopping server at: " + BASE_URI);
      server.shutdownNow();
      useSRService(false, false);
    }
    if (secureServer != null) {
      log.info("Stopping server at: " + BASE_URI_SECURED);
      secureServer.shutdownNow();
      useSRService(true, false);
    }
    System.out.println("Gateway Server(s) stopped");
    System.exit(0);
  }

  static synchronized TypeSafeProperties getProp() {
    try {
      if (prop == null) {
        prop = new TypeSafeProperties();
        File file = new File("config" + File.separator + "app.properties");
        FileInputStream inputStream = new FileInputStream(file);
        prop.load(inputStream);
      }
    } catch (FileNotFoundException ex) {
      throw new ServiceConfigurationError("App.properties file not found, make sure you have the correct working directory set! (directory where "
                                              + "the config folder can be found)", ex);
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return prop;
  }

}
