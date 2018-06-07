/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.common.filter;

import eu.arrowhead.common.Utility;
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

//TODO update it
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
    return method.equals("GET") && (requestTarget.endsWith("orchestration") || requestTarget.endsWith("eventhandler") || requestTarget
        .endsWith("gatekeeper") || requestTarget.endsWith("serviceregistry") || requestTarget.endsWith("mgmt") || requestTarget.endsWith("common")
        || requestTarget.endsWith("store"));
  }

  private boolean isClientAuthorized(String clientCN, String requestTarget, String requestJson) {
    //ServerCN is the Local Cloud keystore
    String serverCN = (String) configuration.getProperty("server_common_name");

    if (requestTarget.contains("gatekeeper") && !SecurityUtils.isTrustStoreCNArrowheadValid(clientCN)) {
      log.info("Client cert does not have 4 parts, so the access will be denied. (GK request)");
      return false;
    } else if (!SecurityUtils.isKeyStoreCNArrowheadValid(clientCN)) {
      log.info("Client cert does not have 5 parts, so the access will be denied.");
      return false;
    }

    if (!requestTarget.contains("gatekeeper")) {
      String[] clientFields = clientCN.split("\\.", 2);
      return serverCN.equalsIgnoreCase(clientFields[1]);
    }

    return true;
    /*// Only the local HMI can use the local cloud management methods
    if (requestTarget.contains("mgmt")) {
      return clientCN.equalsIgnoreCase("sysop." + serverCN);
    }
    // Request to the Orchestrator
    else if (requestTarget.contains("orchestrator")) {
      String[] clientFields = clientCN.split("\\.", 2);
      return serverCN.equalsIgnoreCase(clientFields[1]);
      *//*ServiceRequestForm srf = Utility.fromJson(requestJson, ServiceRequestForm.class);
      // Only the local gatekeeper can send external service request, but the gatekeeper will use the static orch method in this build
      if (srf.getOrchestrationFlags().get("externalServiceRequest")) {
        return false;
      } else {
        // Otherwise all request from the local cloud allowed
        if (!srf.getRequesterSystem().getSystemName().equalsIgnoreCase(clientFields[0])) {
          // BUT the requester system has to be the same as the first part of the common name
          log.error("Requester system name and cert common name do not match!");
          throw new AuthException(
              "Requester system " + srf.getRequesterSystem().getSystemName() + " and cert common name (" + clientCN + ") do not match!",
              Status.UNAUTHORIZED.getStatusCode());
        }

        return serverCN.equalsIgnoreCase(clientFields[1]);
      }*//*
    }
    // Request to the Service Registry
    else if (requestTarget.contains("serviceregistry")) {
      // All requests from the local cloud are allowed, so omit the first part of the client common name (systemName)
      ServiceRegistryEntry entry = Utility.fromJson(requestJson, ServiceRegistryEntry.class);
      String[] clientFields = clientCN.split("\\.", 2);

      if (!entry.getProvider().getSystemName().equalsIgnoreCase(clientFields[0])) {
        // BUT a provider system can only register/remove its own services!
        log.error("Provider system name and cert common name do not match! SR registering/removing denied!");
        throw new AuthException("Provider system " + entry.getProvider().getSystemName() + " and cert common name (" + clientCN + ") do not match!",
                                Status.UNAUTHORIZED.getStatusCode());
      }

      return serverCN.equalsIgnoreCase(clientFields[1]);
    }
    // Request to the Gatekeeper
    else if (requestTarget.contains("gatekeeper")) {
      // Only requests from other Gatekeepers are allowed
      String[] clientFields = clientCN.split("\\.", 3);
      return clientFields.length == 3 && clientFields[2].endsWith("arrowhead.eu");
    }*/
  }

}
