package eu.arrowhead.core.serviceregistry.filter;

import eu.arrowhead.common.Utility;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.security.SecurityUtils;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import org.apache.log4j.Logger;

@Provider
@Priority(Priorities.AUTHORIZATION) //2nd highest priority constant, this filter gets executed after the SecurityFilter
public class AccessControlFilter implements ContainerRequestFilter {

  private static final Logger log = Logger.getLogger(AccessControlFilter.class.getName());
  @Context
  private Configuration configuration;

  @Override
  public void filter(ContainerRequestContext requestContext) {
    SecurityContext sc = requestContext.getSecurityContext();
    String requestTarget = Utility.stripEndSlash(requestContext.getUriInfo().getRequestUri().toString());
    if (sc.isSecure() && !isGetItCalled(requestContext.getMethod(), requestTarget)) {
      String commonName = SecurityUtils.getCertCNFromSubject(sc.getUserPrincipal().getName());
      if (isClientAuthorized(commonName, requestTarget, requestContext.getMethod())) {
        log.info("SSL identification is successful! Cert: " + commonName);
      } else {
        log.error(commonName + " is unauthorized to access " + requestTarget);
        throw new AuthException(commonName + " is unauthorized to access " + requestTarget, Status.UNAUTHORIZED.getStatusCode());
      }
    }
  }

  private boolean isGetItCalled(String method, String requestTarget) {
    return method.equals("GET") && (requestTarget.endsWith("serviceregistry") || requestTarget.endsWith("mgmt"));
  }

  private boolean isClientAuthorized(String clientCN, String requestTarget, String methodType) {
    String serverCN = (String) configuration.getProperty("server_common_name");

    if (!SecurityUtils.isKeyStoreCNArrowheadValid(clientCN)) {
      log.info("Client cert does not have 5 parts, so the access will be denied.");
      return false;
    }

    String[] serverFields = serverCN.split("\\.", 2);
    String[] clientFields = clientCN.split("\\.", 2);
    // serverFields contains: coreSystemName, cloudName.operator.arrowhead.eu
    if (requestTarget.endsWith("register") || requestTarget.endsWith("remove")) {
      // All requests from the local cloud are allowed, so omit the first part of the common names (systemName)
      return serverFields[1].equalsIgnoreCase(clientFields[1]);
    } else if (requestTarget.endsWith("query")) {
      // Only requests from the Orchestrator and Gatekeeper are allowed
      return clientCN.equalsIgnoreCase("orchestrator." + serverFields[1]) || clientCN.equalsIgnoreCase("gatekeeper." + serverFields[1]);
    } //maps legacy register and remove functions, if-else order is important
    else if (methodType.equals("POST") || methodType.equals("PUT")) {
      // All requests from the local cloud are allowed, so omit the first 2 parts of the common names (systemName)
      return serverFields[1].equalsIgnoreCase(clientFields[1]);
    }

    return false;
  }

}
