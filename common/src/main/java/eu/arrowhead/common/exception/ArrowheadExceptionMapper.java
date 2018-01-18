package eu.arrowhead.common.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ArrowheadExceptionMapper implements ExceptionMapper<ArrowheadException> {

  @Override
  public Response toResponse(ArrowheadException ex) {
    ex.printStackTrace();
    ErrorMessage errorMessage = new ErrorMessage(ex.getMessage(), ex.getErrorCode(), ex.getExceptionType(), ex.getOrigin());
    return Response.status(ex.getErrorCode()).entity(errorMessage).header("Content-type", "application/json").build();
  }

}
