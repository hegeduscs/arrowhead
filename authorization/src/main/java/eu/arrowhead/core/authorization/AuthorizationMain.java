/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.authorization;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.Utility;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.ServiceRegistryEntry;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthenticationException;
import eu.arrowhead.common.security.SecurityUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collections;
import java.util.Properties;
import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class AuthorizationMain {

  public static boolean DEBUG_MODE;

  static PrivateKey privateKey;

  private static String SERVICE_REGISTRY_URI = getProp().getProperty("sr_base_uri");
  private static String BASE64_PUBLIC_KEY;
  private static HttpServer server;
  private static HttpServer secureServer;
  private static Properties prop;

  private static final String BASE_URI = getProp().getProperty("base_uri", "http://0.0.0.0:8444/");
  private static final String BASE_URI_SECURED = getProp().getProperty("base_uri_secured", "https://0.0.0.0:8445/");
  private static final Logger log = Logger.getLogger(AuthorizationMain.class.getName());

  public static void main(String[] args) throws IOException {
    PropertyConfigurator.configure("config" + File.separator + "log4j.properties");
    System.out.println("Working directory: " + System.getProperty("user.dir"));
    Utility.isUrlValid(BASE_URI, false);
    Utility.isUrlValid(BASE_URI_SECURED, true);
    if (SERVICE_REGISTRY_URI.startsWith("https")) {
      Utility.isUrlValid(SERVICE_REGISTRY_URI, true);
    } else {
      Utility.isUrlValid(SERVICE_REGISTRY_URI, false);
    }
    if (!SERVICE_REGISTRY_URI.contains("serviceregistry")) {
      SERVICE_REGISTRY_URI = UriBuilder.fromUri(SERVICE_REGISTRY_URI).path("serviceregistry").build().toString();
    }

    KeyStore keyStore = SecurityUtils.loadKeyStore(getProp().getProperty("keystore"), getProp().getProperty("keystorepass"));
    privateKey = SecurityUtils.getPrivateKey(keyStore, getProp().getProperty("keystorepass"));

    boolean daemon = false;
    boolean serverModeSet = false;
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
              server = startServer();
              useSRService(false, true);
              break;
            case "secure":
              secureServer = startSecureServer();
              useSRService(true, true);
              break;
            case "both":
              server = startServer();
              secureServer = startSecureServer();
              useSRService(false, true);
              useSRService(true, true);
              break;
            default:
              log.fatal("Unknown server mode: " + args[i]);
              throw new AssertionError("Unknown server mode: " + args[i]);
          }
      }
    }
    if (!serverModeSet) {
      server = startServer();
      useSRService(false, true);
    }
    Utility.setServiceRegistryUri(SERVICE_REGISTRY_URI);

    //This is here to initialize the database connection before the REST resources are initiated
    DatabaseManager dm = DatabaseManager.getInstance();
    if (daemon) {
      System.out.println("In daemon mode, process will terminate for TERM signal...");
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        System.out.println("Received TERM signal, shutting down...");
        shutdown();
      }));
    } else {
      System.out.println("Type \"stop\" to shutdown Authorization Server(s)...");
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
    log.info("Starting server at: " + BASE_URI);
    System.out.println("Starting insecure server at: " + BASE_URI);

    final ResourceConfig config = new ResourceConfig();
    config.registerClasses(AuthorizationResource.class, AuthorizationApi.class);
    config.packages("eu.arrowhead.common", "eu.arrowhead.core.authorization.filter");

    URI uri = UriBuilder.fromUri(BASE_URI).build();
    final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, config);
    server.getServerConfiguration().setAllowPayloadForUndefinedHttpMethods(true);
    server.start();
    return server;
  }

  private static HttpServer startSecureServer() throws IOException {
    log.info("Starting server at: " + BASE_URI_SECURED);
    System.out.println("Starting secure server at: " + BASE_URI_SECURED);

    final ResourceConfig config = new ResourceConfig();
    config.registerClasses(AuthorizationResource.class, AuthorizationApi.class);
    config.packages("eu.arrowhead.common", "eu.arrowhead.core.authorization.filter");

    String keystorePath = getProp().getProperty("keystore");
    String keystorePass = getProp().getProperty("keystorepass");
    String keyPass = getProp().getProperty("keypass");
    String truststorePath = getProp().getProperty("truststore");
    String truststorePass = getProp().getProperty("truststorepass");

    SSLContextConfigurator sslCon = new SSLContextConfigurator();
    sslCon.setKeyStoreFile(keystorePath);
    sslCon.setKeyStorePass(keystorePass);
    sslCon.setKeyPass(keyPass);
    sslCon.setTrustStoreFile(truststorePath);
    sslCon.setTrustStorePass(truststorePass);
    if (!sslCon.validateConfiguration(true)) {
      log.fatal("SSL Context is not valid, check the certificate files or app.properties!");
      throw new AuthenticationException("SSL Context is not valid, check the certificate files or app.properties!", Status.UNAUTHORIZED
          .getStatusCode(), AuthenticationException.class.getName(), BASE_URI_SECURED);
    }

    SSLContext sslContext = sslCon.createSSLContext();
    Utility.setSSLContext(sslContext);

    KeyStore keyStore = SecurityUtils.loadKeyStore(keystorePath, keystorePass);
    X509Certificate serverCert = SecurityUtils.getFirstCertFromKeyStore(keyStore);
    BASE64_PUBLIC_KEY = Base64.getEncoder().encodeToString(serverCert.getPublicKey().getEncoded());
    System.out.println("Server PublicKey Base64: " + BASE64_PUBLIC_KEY);
    String serverCN = SecurityUtils.getCertCNFromSubject(serverCert.getSubjectDN().getName());
    if (!SecurityUtils.isKeyStoreCNArrowheadValid(serverCN)) {
      log.fatal("Server CN is not compliant with the Arrowhead cert structure, since it does not have 6 parts.");
      throw new AuthenticationException("Server CN ( " + serverCN + ") is not compliant with the Arrowhead cert structure, since it does not have 6 parts.",
                                        Status.UNAUTHORIZED.getStatusCode(), AuthenticationException.class.getName(), BASE_URI_SECURED);
    }
    log.info("Certificate of the secure server: " + serverCN);
    config.property("server_common_name", serverCN);

    URI uri = UriBuilder.fromUri(BASE_URI_SECURED).build();
    final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, config, true, new SSLEngineConfigurator(sslCon).setClientMode(false).setNeedClientAuth(true));
    server.getServerConfiguration().setAllowPayloadForUndefinedHttpMethods(true);
    server.start();
    return server;
  }

  private static void useSRService(boolean isSecure, boolean registering) {
    URI uri = isSecure ? UriBuilder.fromUri(BASE_URI_SECURED).build() : UriBuilder.fromUri(BASE_URI).build();
    ArrowheadSystem authSystem = new ArrowheadSystem("authorization", uri.getHost(), uri.getPort(), BASE64_PUBLIC_KEY);
    ArrowheadService authControlService = new ArrowheadService(Utility.createSD(Utility.AUTH_CONTROL_SERVICE, isSecure), Collections.singletonList
        ("JSON"), null);
    ArrowheadService tokenGenerationService = new ArrowheadService(Utility.createSD(Utility.TOKEN_GEN_SERVICE, isSecure), Collections.singletonList("JSON"), null);
    if (isSecure) {
      authControlService.setServiceMetadata(Utility.secureServerMetadata);
      tokenGenerationService.setServiceMetadata(Utility.secureServerMetadata);
    }

    //Preparing the payloads
    ServiceRegistryEntry authControlEntry = new ServiceRegistryEntry(authControlService, authSystem, "authorization");
    ServiceRegistryEntry tokenGenEntry = new ServiceRegistryEntry(tokenGenerationService, authSystem, "authorization/token");

    if (registering) {
      try {
        Utility.sendRequest(UriBuilder.fromUri(SERVICE_REGISTRY_URI).path("register").build().toString(), "POST", authControlEntry);
      } catch (ArrowheadException e) {
        if (e.getExceptionType().contains("DuplicateEntryException")) {
          Utility.sendRequest(UriBuilder.fromUri(SERVICE_REGISTRY_URI).path("remove").build().toString(), "PUT", authControlEntry);
          Utility.sendRequest(UriBuilder.fromUri(SERVICE_REGISTRY_URI).path("register").build().toString(), "POST", authControlEntry);
        } else {
          System.out.println("Authorization control service registration failed.");
        }
      }
      try {
        Utility.sendRequest(UriBuilder.fromUri(SERVICE_REGISTRY_URI).path("register").build().toString(), "POST", tokenGenEntry);
      } catch (ArrowheadException e) {
        if (e.getExceptionType().contains("DuplicateEntryException")) {
          Utility.sendRequest(UriBuilder.fromUri(SERVICE_REGISTRY_URI).path("remove").build().toString(), "PUT", tokenGenEntry);
          Utility.sendRequest(UriBuilder.fromUri(SERVICE_REGISTRY_URI).path("register").build().toString(), "POST", tokenGenEntry);
        } else {
          System.out.println("Token generation service registration failed.");
        }
      }
    } else {
      Utility.sendRequest(UriBuilder.fromUri(SERVICE_REGISTRY_URI).path("remove").build().toString(), "PUT", authControlEntry);
      Utility.sendRequest(UriBuilder.fromUri(SERVICE_REGISTRY_URI).path("remove").build().toString(), "PUT", tokenGenEntry);
      System.out.println("Authorization services deregistered.");
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
    System.out.println("Authorization Server(s) stopped");
  }

  public static synchronized Properties getProp() {
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
