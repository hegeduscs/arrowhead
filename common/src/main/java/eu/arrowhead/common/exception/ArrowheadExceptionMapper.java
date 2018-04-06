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
import javax.ws.rs.ext.Provider;
import org.apache.log4j.Logger;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;

@Provider
public class ArrowheadExceptionMapper implements ExceptionMapper<ArrowheadException> {

  @Inject
  private javax.inject.Provider<ContainerRequest> requestContext;
  @Inject
  private javax.inject.Provider<ContainerResponse> responseContext;

  private static final Logger log = Logger.getLogger(ArrowheadExceptionMapper.class.getName());

  @Override
  public Response toResponse(ArrowheadException ex) {
    ex.printStackTrace();
    String origin = (ex.getOrigin() == null && requestContext.get() != null) ? requestContext.get().getAbsolutePath().toString() : ex.getOrigin();
    int errorCode = (ex.getErrorCode() == 0 && responseContext.get() != null) ? responseContext.get().getStatus() : ex.getErrorCode();
    if (errorCode == 0) {
      switch (ex.getExceptionType()) {
        case AUTH:
          errorCode = Status.UNAUTHORIZED.getStatusCode();
          break;
        case BAD_PAYLOAD:
          errorCode = Status.BAD_REQUEST.getStatusCode();
          log.error("BadPayloadException at: " + origin);
          break;
        case DATA_NOT_FOUND:
          errorCode = Status.NOT_FOUND.getStatusCode();
          break;
        case DUPLICATE_ENTRY:
          errorCode = Status.BAD_REQUEST.getStatusCode();
          break;
        case JSON_PROCESSING:
          errorCode = Status.BAD_REQUEST.getStatusCode();
          break;
        case UNAVAILABLE:
          errorCode = Status.GATEWAY_TIMEOUT.getStatusCode();
          break;
        default:
          errorCode = Status.INTERNAL_SERVER_ERROR.getStatusCode();
      }
    }

    ErrorMessage errorMessage = new ErrorMessage(ex.getMessage(), errorCode, ex.getExceptionType(), origin);
    return Response.status(errorCode).entity(errorMessage).header("Content-type", "application/json").build();
  }

}
