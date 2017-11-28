package eu.arrowhead.common.exception;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.jetbrains.annotations.NotNull;

@Provider
public class BadURIExceptionMapper implements ExceptionMapper<NotFoundException> {

  public Response toResponse(@NotNull NotFoundException exception) {
    exception.printStackTrace();
    ErrorMessage errorMessage = new ErrorMessage("Bad request: requested URI does not exist.", 404, NotFoundException.class.toString());
    return Response.status(Response.Status.NOT_FOUND).entity(errorMessage).header("Content-type", "application/json").build();
  }
}
