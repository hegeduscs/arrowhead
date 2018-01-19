package eu.arrowhead.common.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

public class GenericExceptionMapper implements ExceptionMapper<RuntimeException> {

  @Override
  public Response toResponse(RuntimeException ex) {
    ex.printStackTrace();
    ErrorMessage errorMessage = new ErrorMessage(ex.getMessage(), 500, ex.getClass().getName(), null);
    return Response.status(Status.INTERNAL_SERVER_ERROR).entity(errorMessage).header("Content-type", "application/json").build();
  }
}
