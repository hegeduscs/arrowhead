package eu.arrowhead.core.authorization;

import eu.arrowhead.common.Utility;
import eu.arrowhead.common.exception.AuthenticationException;
import eu.arrowhead.common.security.SecurityUtils;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import org.apache.log4j.Logger;

@Provider
@Priority(Priorities.AUTHORIZATION) //2nd highest priority constant, this filter gets executed after the SecurityFilter
public class AccessControlFilter implements ContainerRequestFilter {

  private static Logger log = Logger.getLogger(AccessControlFilter.class.getName());
  @Context
  private static Configuration configuration;

  @Override
  public void filter(ContainerRequestContext requestContext) {
    SecurityContext sc = requestContext.getSecurityContext();
    String requestTarget = Utility.stripEndSlash(requestContext.getUriInfo().getRequestUri().toString());
    boolean isGetItCalled = requestContext.getMethod().equals("GET") && requestTarget.endsWith("authorization");
    if (sc.isSecure() && !isGetItCalled) {
      String subjectName = sc.getUserPrincipal().getName();
      if (isClientAuthorized(subjectName)) {
        log.info("SSL identification is successful! Cert: " + subjectName);
      } else {
        log.error(SecurityUtils.getCertCNFromSubject(subjectName) + " is unauthorized to access " + requestTarget);
        throw new AuthenticationException(SecurityUtils.getCertCNFromSubject(subjectName) + " is unauthorized to access " + requestTarget);
      }
    }
  }

  private static boolean isClientAuthorized(String subjectName) {
    String clientCN = SecurityUtils.getCertCNFromSubject(subjectName);
    String serverCN = (String) configuration.getProperty("server_common_name");

    if (!SecurityUtils.isCommonNameArrowheadValid(clientCN)) {
      log.info("Client cert does not have 6 parts, so the access will be denied.");
      return false;
    }
    // Only requests from the Orchestrator and Gatekeeper are allowed
    String[] serverFields = serverCN.split("\\.", 2);
    // serverFields contains: coreSystemName, coresystems.cloudName.operator.arrowhead.eu

    // If this is true, then the certificate is from the local Orchestrator or Gatekeeper
    return clientCN.equalsIgnoreCase("orchestrator" + serverFields[1]) || clientCN.equalsIgnoreCase("gatekeeper" + serverFields[1]);
  }

}
