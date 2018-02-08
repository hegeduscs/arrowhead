/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.common.exception;

import javax.inject.Inject;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.glassfish.jersey.server.ContainerRequest;

@Provider
public class BadMethodExceptionMapper implements ExceptionMapper<NotAllowedException> {

  @Inject
  private javax.inject.Provider<ContainerRequest> requestContext;

  public Response toResponse(NotAllowedException ex) {
    ex.printStackTrace();
    ErrorMessage errorMessage;
    if (ex.getMessage() != null) {
      errorMessage = new ErrorMessage(ex.getMessage(), 405, NotAllowedException.class.getName(), null);
    } else {
      errorMessage = new ErrorMessage(requestContext.get().getMethod() + " is not allowed at " + requestContext.get().getPath(true), 405,
          NotAllowedException.class.getName(), requestContext.get().getBaseUri().toString());
    }

    return Response.status(Status.METHOD_NOT_ALLOWED).entity(errorMessage).header("Content-type", "application/json").build();
  }
}
