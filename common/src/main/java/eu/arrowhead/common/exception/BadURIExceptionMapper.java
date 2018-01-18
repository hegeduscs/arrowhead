package eu.arrowhead.common.exception;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class BadURIExceptionMapper implements ExceptionMapper<NotFoundException> {

  public Response toResponse(NotFoundException ex) {
    ex.printStackTrace();
    ErrorMessage errorMessage = new ErrorMessage("Bad request: requested URI does not exist.", 404, NotFoundException.class.toString(), null);
    return Response.status(Response.Status.NOT_FOUND).entity(errorMessage).header("Content-type", "application/json").build();
  }
}
