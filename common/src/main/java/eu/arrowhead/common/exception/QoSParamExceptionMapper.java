package eu.arrowhead.common.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author ID0084D
 */
@Provider
public class QoSParamExceptionMapper implements ExceptionMapper<QoSParamException> {

  @Override
  public Response toResponse(QoSParamException ex) {
    ErrorMessage errorMessage = new ErrorMessage(ex.getMessage(), 400);
    return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).header("Content-type", "application/json").build();
  }

}
