package eu.arrowhead.common.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class DuplicateEntryExceptionMapper implements ExceptionMapper<DuplicateEntryException> {

  @Override
  public Response toResponse(DuplicateEntryException ex) {
    ex.printStackTrace();
    ErrorMessage errorMessage = new ErrorMessage(ex.getMessage(), 400);
    return Response.status(Status.BAD_REQUEST)
        .entity(errorMessage)
        .build();
  }

}
