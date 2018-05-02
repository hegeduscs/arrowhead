/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.common.filter;

import eu.arrowhead.common.exception.ErrorMessage;
import eu.arrowhead.common.exception.ExceptionType;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.ENTITY_CODER)
public class EmptyPayloadFilter implements ContainerRequestFilter {

  @Override
  public void filter(ContainerRequestContext requestContext) {
    String method = requestContext.getMethod();
    int contentLength = requestContext.getLength();
    if ((method.equals("POST") || method.equals("PUT")) && contentLength == 0) {
      ErrorMessage em = new ErrorMessage(
          "Message body is null (unusual for POST/PUT request)! If you truly want to send an empty payload, try sending empty brackets: {} for "
              + "JSONObject, [] for JSONArray.", 400, ExceptionType.BAD_PAYLOAD, requestContext.getUriInfo().getAbsolutePath().toString());
      requestContext.abortWith(Response.status(Status.BAD_REQUEST).entity(em).build());
    }
    if ((method.equals(("GET")) || method.equals("DELETE")) && contentLength > 0) {
      ErrorMessage em = new ErrorMessage("Message body is not null (unusual for GET/DELETE request)!", 400, ExceptionType.BAD_PAYLOAD,
                                         requestContext.getUriInfo().getAbsolutePath().toString());
      requestContext.abortWith(Response.status(Status.BAD_REQUEST).entity(em).build());
    }

  }

}
