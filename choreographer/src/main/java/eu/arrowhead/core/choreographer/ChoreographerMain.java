/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.choreographer;

import eu.arrowhead.common.misc.TypeSafeProperties;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ServiceConfigurationError;
import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;

public class ChoreographerMain {

  public static boolean DEBUG_MODE;

  private static String BASE_URI;
  private static String SR_BASE_URI;
  private static String BASE64_PUBLIC_KEY;
  private static HttpServer server;
  private static HttpServer secureServer;
  private static TypeSafeProperties prop;

  private static final Logger log = Logger.getLogger(ChoreographerMain.class.getName());

  public static void main(String[] args) {
    System.out.println("w00t w00t");
  }

  private static void shutdown() {
    if (server != null) {
      log.info("Stopping server at: " + BASE_URI);
      server.shutdownNow();
    }
    System.out.println("Choreographer Server stopped");
    System.exit(0);
  }

  private static synchronized TypeSafeProperties getProp() {
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
