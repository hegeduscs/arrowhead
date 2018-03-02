/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.authorization.filter;

import eu.arrowhead.common.Utility;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.security.SecurityUtils;
import eu.arrowhead.core.authorization.AuthorizationMain;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import org.apache.log4j.Logger;

//Legacy version of the AccessControlFilter, with certificates containing the system group field
//@Provider Uncomment this line to activate the filter
@Priority(Priorities.AUTHORIZATION) //2nd highest priority constant, this filter gets executed after the SecurityFilter
public class SupportAccessControlFilter implements ContainerRequestFilter {

  private static final Logger log = Logger.getLogger(SupportAccessControlFilter.class.getName());
  @Context
  private Configuration configuration;

  @Override
  public void filter(ContainerRequestContext requestContext) {
    SecurityContext sc = requestContext.getSecurityContext();
    String requestTarget = Utility.stripEndSlash(requestContext.getUriInfo().getRequestUri().toString());
    if (sc.isSecure() && !isGetItCalled(requestContext.getMethod(), requestTarget)) {
      String commonName = SecurityUtils.getCertCNFromSubject(sc.getUserPrincipal().getName());
      if (isClientAuthorized(commonName, requestTarget)) {
        log.info("SSL identification is successful! Cert: " + commonName);
      } else {
        log.error(commonName + " is unauthorized to access " + requestTarget);
        throw new AuthException(commonName + " is unauthorized to access " + requestTarget, Status.UNAUTHORIZED.getStatusCode());
      }
    }
  }

  private boolean isGetItCalled(String method, String requestTarget) {
    return method.equals("GET") && (requestTarget.endsWith("authorization") || requestTarget.endsWith("mgmt"));
  }

  private boolean isClientAuthorized(String clientCN, String requestTarget) {
    String serverCN = (String) configuration.getProperty("server_common_name");

    if (!SecurityUtils.isKeyStoreCNArrowheadValidLegacy(clientCN)) {
      log.info("Client cert does not have 6 parts, so the access will be denied.");
      return false;
    }

    String[] serverFields = serverCN.split("\\.", 2);
    // serverFields contains: coreSystemName, cloudName.operator.arrowhead.eu
    if (requestTarget.contains("mgmt")) {
      // Only the local HMI can use these methods
      return clientCN.equalsIgnoreCase("hmi." + serverFields[1]);
    } else {
      // If this property is true, then every system from the local cloud can use the auth services
      if (Boolean.valueOf(AuthorizationMain.getProp().getProperty("enable_auth_for_cloud"))) {
        String[] clientFields = clientCN.split("\\.", 2);
        return serverFields[1].equalsIgnoreCase(clientFields[1]);
      }
      // If it is not true, only the Orchestrator and Gatekeeper can use it
      else {
        return clientCN.equalsIgnoreCase("orchestrator." + serverFields[1]) || clientCN.equalsIgnoreCase("gatekeeper." + serverFields[1]);
      }
    }
  }

}
