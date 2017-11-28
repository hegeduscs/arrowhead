package eu.arrowhead.common.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.jetbrains.annotations.NotNull;

@Provider
public class AuthenticationExceptionMapper implements ExceptionMapper<AuthenticationException> {

  @Override
  public Response toResponse(@NotNull AuthenticationException ex) {
    ex.printStackTrace();
    ErrorMessage errorMessage = new ErrorMessage(ex.getMessage(), 401, AuthenticationException.class.toString());
    return Response.status(Status.UNAUTHORIZED).entity(errorMessage).header("Content-type", "application/json").build();
  }

}
