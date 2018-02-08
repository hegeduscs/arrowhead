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
import org.glassfish.jersey.server.ContainerResponse;

@Provider
public class ArrowheadExceptionMapper implements ExceptionMapper<ArrowheadException> {

  @Inject
  private javax.inject.Provider<ContainerResponse> responseContext;

  @Override
  public Response toResponse(ArrowheadException ex) {
    ex.printStackTrace();
    int errorCode = (ex.getErrorCode() == 0 && responseContext.get() != null) ? responseContext.get().getStatus() : ex.getErrorCode();
    String origin = (ex.getOrigin() == null && responseContext.get() != null) ? responseContext.get().getRequestContext().getAbsolutePath().toString()
        : ex.getOrigin();
    ErrorMessage errorMessage = new ErrorMessage(ex.getMessage(), errorCode, ex.getExceptionType(), origin);
    return Response.status(ex.getErrorCode()).entity(errorMessage).header("Content-type", "application/json").build();
  }

}
