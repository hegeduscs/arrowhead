package eu.arrowhead.common.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class DataNotFoundExceptionMapper implements ExceptionMapper<DataNotFoundException> {

  @Override
  public Response toResponse(DataNotFoundException ex) {
    ex.printStackTrace();
    ErrorMessage errorMessage = new ErrorMessage(ex.getMessage(), 404);
    return Response.status(Status.NOT_FOUND)
        .entity(errorMessage)
        .build();
  }

}
