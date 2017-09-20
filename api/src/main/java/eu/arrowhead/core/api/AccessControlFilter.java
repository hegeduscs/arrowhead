package eu.arrowhead.core.api;

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
  private Configuration configuration;

  @Override
  public void filter(ContainerRequestContext requestContext) {
    SecurityContext sc = requestContext.getSecurityContext();
    //TODO add 4 condition for the if, for the 4 getit resource in the auth/common/config/orch api
    if (sc.isSecure()) {
      String subjectName = sc.getUserPrincipal().getName();
      if (isClientAuthorized(subjectName)) {
        log.info("SSL identification is successful! Cert: " + subjectName);
      } else {
        String requestTarget = requestContext.getUriInfo().getRequestUri().toString();
        log.error(SecurityUtils.getCertCNFromSubject(subjectName) + " is unauthorized to access " + requestTarget);
        throw new AuthenticationException(SecurityUtils.getCertCNFromSubject(subjectName) + " is unauthorized to access " + requestTarget);
      }
    }
  }

  private boolean isClientAuthorized(String subjectName) {
    String clientCN = SecurityUtils.getCertCNFromSubject(subjectName);
    String serverCN = (String) configuration.getProperty("server_common_name");

    if (!SecurityUtils.isCommonNameArrowheadValid(clientCN)) {
      log.info("Client cert does not have 6 parts, so the access will be denied.");
      return false;
    }
    // All requests from the local cloud are allowed, so omit the first 2 parts of the common names (systemName.systemGroup)
    String[] serverFields = serverCN.split("\\.", 3);
    String[] clientFields = clientCN.split("\\.", 3);
    // serverFields contains: coreSystemName, coresystems, cloudName.operator.arrowhead.eu

    // If this is true, then the certificates are from the same local cloud
    return serverFields[2].equalsIgnoreCase(clientFields[2]);
  }

}
