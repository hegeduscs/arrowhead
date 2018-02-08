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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import org.glassfish.jersey.server.ContainerResponse;

public class GenericExceptionMapper implements ExceptionMapper<RuntimeException> {

  @Inject
  private javax.inject.Provider<ContainerResponse> responseContext;

  @Override
  public Response toResponse(RuntimeException ex) {
    ex.printStackTrace();
    ErrorMessage errorMessage = new ErrorMessage(ex.getMessage(), responseContext.get().getStatus(), ex.getClass().getName(),
        responseContext.get().getRequestContext().getAbsolutePath().toString());
    return Response.status(Status.INTERNAL_SERVER_ERROR).entity(errorMessage).header("Content-type", "application/json").build();
  }
}
