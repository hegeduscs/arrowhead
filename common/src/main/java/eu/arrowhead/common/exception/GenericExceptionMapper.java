package eu.arrowhead.common.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<ArrowheadException> {

  @Override
  public Response toResponse(ArrowheadException ex) {
    ex.printStackTrace();
    ErrorMessage errorMessage = new ErrorMessage("Class: " + ex.getClass().toString() + " Message: " + ex.getMessage(), ex.getErrorCode(),
                                                 ArrowheadException.class.toString());
    return Response.status(Status.INTERNAL_SERVER_ERROR).entity(errorMessage).header("Content-type", "application/json").build();
  }

}
