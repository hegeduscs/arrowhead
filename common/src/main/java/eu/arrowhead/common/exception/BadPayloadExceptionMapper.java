package eu.arrowhead.common.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class BadPayloadExceptionMapper implements ExceptionMapper<BadPayloadException> {

  @Override
  public Response toResponse(BadPayloadException ex) {
    ex.printStackTrace();
    ErrorMessage errorMessage = new ErrorMessage(ex.getMessage(), ex.getErrorCode(), BadPayloadException.class.toString());
    return Response.status(Status.BAD_REQUEST).entity(errorMessage).header("Content-type", "application/json").build();
  }

}
