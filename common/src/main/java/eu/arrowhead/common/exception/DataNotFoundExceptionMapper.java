package eu.arrowhead.common.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.jetbrains.annotations.NotNull;

@Provider
public class DataNotFoundExceptionMapper implements ExceptionMapper<DataNotFoundException> {

  @Override
  public Response toResponse(@NotNull DataNotFoundException ex) {
    ex.printStackTrace();
    ErrorMessage errorMessage = new ErrorMessage(ex.getMessage(), 404, DataNotFoundException.class.toString());
    return Response.status(Status.NOT_FOUND).entity(errorMessage).header("Content-type", "application/json").build();
  }

}
