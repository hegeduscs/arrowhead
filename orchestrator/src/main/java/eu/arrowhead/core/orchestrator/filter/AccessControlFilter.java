/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.orchestrator.filter;

import eu.arrowhead.common.Utility;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.messages.ServiceRequestForm;
import eu.arrowhead.common.security.SecurityUtils;
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
    return method.equals("GET") && (requestTarget.endsWith("orchestration") || requestTarget.endsWith("mgmt/common") || requestTarget
        .endsWith("mgmt/store"));
  }

  private boolean isClientAuthorized(String clientCN, String requestTarget, String requestJson) {
    String serverCN = (String) configuration.getProperty("server_common_name");

    if (!SecurityUtils.isKeyStoreCNArrowheadValid(clientCN)) {
      log.info("Client cert does not have 5 parts, so the access will be denied. Make sure the field values do not contain dots!");
      return false;
    }

    String[] serverFields = serverCN.split("\\.", 2);
    String[] clientFields = clientCN.split("\\.", 2);
    // serverFields contains: coreSystemName, cloudName.operator.arrowhead.eu
    if (requestTarget.contains("mgmt")) {
      // Only the local HMI can use these methods
      return clientCN.equalsIgnoreCase("hmi." + serverFields[1]);
    } else if (requestTarget.contains("store")) {
      // Only requests from the local cloud are allowed
      return serverFields[1].equalsIgnoreCase(clientFields[1]);
    } else {
      ServiceRequestForm srf = Utility.fromJson(requestJson, ServiceRequestForm.class);

      // If this is an external service request, only the local Gatekeeper can send this method
      if (srf.getOrchestrationFlags().get("externalServiceRequest")) {
        return clientFields[0].equalsIgnoreCase("gatekeeper") && serverFields[1].equalsIgnoreCase(clientFields[1]);
      } else {
        // Otherwise all request from the local cloud are allowed
        if (!srf.getRequesterSystem().getSystemName().equalsIgnoreCase(clientFields[0])) {
          // BUT the requester system has to be the same as the first part of the common name
          log.error("Requester system name and cert common name do not match!");
          throw new AuthException("Requester system " + srf.getRequesterSystem().getSystemName() + " and cert common name (" + clientCN + ") do not match!",
                                  Status.UNAUTHORIZED.getStatusCode());
        }

        return serverFields[1].equalsIgnoreCase(clientFields[1]);
      }
    }
  }

}
