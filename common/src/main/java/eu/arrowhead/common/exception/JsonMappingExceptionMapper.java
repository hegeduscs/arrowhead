package eu.arrowhead.common.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class JsonMappingExceptionMapper implements ExceptionMapper<JsonMappingException> {

  @Override
  public Response toResponse(JsonMappingException ex) {
    ex.printStackTrace();
    ErrorMessage errorMessage = new ErrorMessage(ex.getMessage(), 400, JsonMappingException.class.toString());
    return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
  }

}
