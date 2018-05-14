/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.core.serviceregistry_sql.filter;

import eu.arrowhead.common.Utility;
import eu.arrowhead.common.database.ServiceRegistryEntry;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.misc.SecurityUtils;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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
      String requestJson = Utility.getRequestPayload(requestContext.getEntityStream());
      String commonName = SecurityUtils.getCertCNFromSubject(sc.getUserPrincipal().getName());
      if (isClientAuthorized(commonName, requestTarget, requestJson)) {
        log.info("SSL identification is successful! Cert: " + commonName);
      } else {
        log.error(commonName + " is unauthorized to access " + requestTarget);
        throw new AuthException(commonName + " is unauthorized to access " + requestTarget, Status.UNAUTHORIZED.getStatusCode());
      }

      try {
        InputStream in = new ByteArrayInputStream(requestJson.getBytes("UTF-8"));
        requestContext.setEntityStream(in);
      } catch (UnsupportedEncodingException e) {
        log.fatal("AccessControlFilter String.getBytes() has unsupported charset set!");
        throw new AssertionError("AccessControlFilter String.getBytes() has unsupported charset set! Code needs to be changed!", e);
      }
    }
  }

  private boolean isGetItCalled(String method, String requestTarget) {
    return method.equals("GET") && (requestTarget.endsWith("serviceregistry") || requestTarget.endsWith("mgmt"));
  }

  private boolean isClientAuthorized(String clientCN, String requestTarget, String requestJson) {
    String serverCN = (String) configuration.getProperty("server_common_name");

    if (!SecurityUtils.isKeyStoreCNArrowheadValid(clientCN)) {
      log.info("Client cert does not have 5 parts, so the access will be denied. Make sure the field values do not contain dots!");
      return false;
    }

    String[] serverFields = serverCN.split("\\.", 2);
    // serverFields contains: coreSystemName, cloudName.operator.arrowhead.eu
    if (requestTarget.contains("mgmt")) {

      //Only the local System Operator can use these methods
      return clientCN.equalsIgnoreCase("sysop." + serverFields[1]);
    } else if (requestTarget.endsWith("register") || requestTarget.endsWith("remove")) {

      // All requests from the local cloud are allowed
      ServiceRegistryEntry entry = Utility.fromJson(requestJson, ServiceRegistryEntry.class);
      String[] clientFields = clientCN.split("\\.", 2);

      if (!entry.getProvider().getSystemName().equalsIgnoreCase(clientFields[0])) {
        // BUT a provider system can only register/remove its own services!
        log.error("Provider system name and cert common name do not match! SR registering/removing denied!");
        throw new AuthException("Provider system " + entry.getProvider().getSystemName() + " and cert common name (" + clientCN + ") do not match!",
                                Status.UNAUTHORIZED.getStatusCode());
      }

      return serverFields[1].equalsIgnoreCase(clientFields[1]);
    } else if (requestTarget.endsWith("query")) {

      // Only requests from the local Orchestrator and Gatekeeper are allowed
      return clientCN.equalsIgnoreCase("orchestrator." + serverFields[1]) || clientCN.equalsIgnoreCase("gatekeeper." + serverFields[1]);
    }

    return false;
  }

}
