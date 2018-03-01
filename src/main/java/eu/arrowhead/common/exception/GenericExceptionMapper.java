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
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {

  @Inject
  private javax.inject.Provider<ContainerRequest> requestContext;
  @Inject
  private javax.inject.Provider<ContainerResponse> responseContext;

  @Override
  public Response toResponse(Exception ex) {
    ex.printStackTrace();
    int errorCode = 500; //Internal Server Error
    String origin = null;
    if (requestContext.get() != null) {
      origin = requestContext.get().getAbsolutePath().toString();
    }
    if (responseContext.get() != null) {
      errorCode = responseContext.get().getStatus();
    }
    ErrorMessage errorMessage = new ErrorMessage(ex.getMessage(), errorCode, ex.getClass().getName(), origin);
    return Response.status(errorCode).entity(errorMessage).header("Content-type", "application/json").build();
  }
}
