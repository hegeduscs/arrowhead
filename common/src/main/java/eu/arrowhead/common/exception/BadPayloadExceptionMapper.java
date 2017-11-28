package eu.arrowhead.common.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.jetbrains.annotations.NotNull;

@Provider
public class BadPayloadExceptionMapper implements ExceptionMapper<BadPayloadException> {

  @Override
  public Response toResponse(@NotNull BadPayloadException ex) {
    ex.printStackTrace();
    ErrorMessage errorMessage = new ErrorMessage(ex.getMessage(), 400, BadPayloadException.class.toString());
    return Response.status(Status.BAD_REQUEST).entity(errorMessage).header("Content-type", "application/json").build();
  }

}
