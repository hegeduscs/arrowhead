package eu.arrowhead.common.exception;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.glassfish.jersey.server.ContainerRequest;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

  @Inject
  private javax.inject.Provider<ContainerRequest> requestContext;

  @Override
  public Response toResponse(ConstraintViolationException exception) {
    exception.printStackTrace();
    int errorCode = 404; //Bad Request
    String origin = requestContext.get() != null ? requestContext.get().getAbsolutePath().toString() : "unknown";

    ErrorMessage errorMessage = new ErrorMessage(exception.getMessage(), errorCode, ExceptionType.VALIDATION, origin);
    return Response.status(errorCode).entity(errorMessage).header("Content-type", "application/json").build();
  }
}
