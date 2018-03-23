/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.common.misc;

import eu.arrowhead.common.exception.ErrorMessage;
import eu.arrowhead.common.exception.ExceptionType;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class EmptyPayloadFilter implements ContainerRequestFilter {

  @Override
  public void filter(ContainerRequestContext requestContext) {
    String method = requestContext.getMethod();
    if (method.equals("POST") || method.equals("PUT")) {
      int contentLength = requestContext.getLength();
      if (contentLength == 0) {
        ErrorMessage em = new ErrorMessage(
            "Payload is null (unusual for POST/PUT request)! If you want to send an empty payload, try sending empty brackets ({})", 400,
            ExceptionType.GENERIC, requestContext.getUriInfo().getAbsolutePath().toString());
        requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST).entity(em).build());
      }
    }
  }

}
