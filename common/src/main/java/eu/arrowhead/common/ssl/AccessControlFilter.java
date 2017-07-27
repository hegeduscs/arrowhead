package eu.arrowhead.common.ssl;

import java.io.IOException;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import org.apache.log4j.Logger;

@Provider
@Priority(Priorities.AUTHORIZATION)
//2nd highest priority constant, this filter gets executed after the SecurityFilter
public class AccessControlFilter implements ContainerRequestFilter {

  private static Logger log = Logger.getLogger(AccessControlFilter.class.getName());
  @Context
  Configuration configuration;
  @Inject
  javax.inject.Provider<UriInfo> uriInfo;

  private static boolean isClientAuthorized(SecurityContext sc, Configuration configuration,
      boolean onlyFromOrchestrator) {
    String subjectname = sc.getUserPrincipal().getName();
    String clientCN = SecurityUtils.getCertCNFromSubject(subjectname);
    String serverCN = (String) configuration.getProperty("server_common_name");

    String[] serverFields = serverCN.split("\\.", -1);
    String[] clientFields = clientCN.split("\\.", -1);
    String allowedCN = "orchestrator.coresystems";
    String serverCNend = "";
    String clientCNend = "";
    if (serverFields.length < 3 || clientFields.length < 3) {
      log.info("SSL error: one of the CNs have less than 3 fields!");
      return false;
    } else {
      for (int i = 2; i < serverFields.length; i++) {
        serverCNend = serverCNend.concat("." + serverFields[i]);
        allowedCN = allowedCN.concat("." + serverFields[i]);
      }

      for (int i = 2; i < clientFields.length; i++) {
        clientCNend = clientCNend.concat("." + clientFields[i]);
      }
    }

    //If we only accept requests from the Orchestrator
    if (onlyFromOrchestrator) {
      if (!clientCN.equalsIgnoreCase(allowedCN)) {
        log.info("SSL error: common names are not equal!");
        return false;
      }
    }
    //If we accept requests from anywhere in the local cloud
    else {
      if (!clientCNend.equalsIgnoreCase(serverCNend)) {
        log.info("SSL error: common names are not equal!");
        return false;
      }
    }

    return true;
  }

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {

    SecurityContext sc = requestContext.getSecurityContext();
    if (sc.isSecure()) {
      log.info("Got a request from a secure channel. Cert: " + sc.getUserPrincipal().getName());
      String requestTarget = uriInfo.get().getAbsolutePath().getPath();
      if (requestTarget.contains("authorization") || requestTarget.contains("init")) {
        if (isClientAuthorized(sc, configuration, true)) {
          log.info("Identification is successful! (SSL)");
          return;
        } else {
          log.info("Unauthorized access! (SSL)");
          /*throw new AuthenticationException
					("This client is not allowed to use this resource: " + requestTarget);*/
        }
      } else {
        if (isClientAuthorized(sc, configuration, false)) {
          log.info("Identification is successful! (SSL)");
          return;
        } else {
          log.info("Unauthorized access! (SSL)");
					/*throw new AuthenticationException
					("This client is not allowed to use this resource: " + requestTarget);*/
        }
      }
    }
  }

}
