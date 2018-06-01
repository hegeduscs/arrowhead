/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.core.eventhandler.filter;

import eu.arrowhead.common.Utility;
import eu.arrowhead.common.database.EventFilter;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.messages.PublishEvent;
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
    return method.equals("GET") && (requestTarget.endsWith("eventhandler"));
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
    if (requestTarget.contains("publish")) {
      PublishEvent event = Utility.fromJson(requestJson, PublishEvent.class);
      if (!clientFields[0].equalsIgnoreCase(event.getSource().getSystemName())) {
        log.error("Source system name and cert common name do not match! Event publishing denied!");
        throw new AuthException("Source system " + event.getSource().getSystemName() + " and cert common name (" + clientCN + ") do not match!",
                                Status.UNAUTHORIZED.getStatusCode());
      }
    } else if (requestTarget.endsWith("subscription")) {
      EventFilter filter = Utility.fromJson(requestJson, EventFilter.class);
      if (!clientFields[0].equalsIgnoreCase(filter.getConsumer().getSystemName())) {
        log.error("Consumer system name and cert common name do not match! Event subscription/unsubscribe denied!");
        throw new AuthException("Consumer system " + filter.getConsumer().getSystemName() + " and cert common name (" + clientCN + ") do not match!",
                                Status.UNAUTHORIZED.getStatusCode());
      }
    } else {
      //Only the DELETE method based unsubscribe method left
      String[] uriParts = requestTarget.split("/");
      if (!clientFields[0].equalsIgnoreCase(uriParts[uriParts.length - 1])) {
        log.error("Consumer system name and cert common name do not match! Event unsubscribe denied!");
        throw new AuthException("Consumer system " + uriParts[uriParts.length - 1] + " and cert common name (" + clientCN + ") do not match!",
                                Status.UNAUTHORIZED.getStatusCode());
      }
    }

    // All requests from the local cloud are allowed
    return serverFields[1].equalsIgnoreCase(clientFields[1]);
  }

}
