package eu.arrowhead.common.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {

	@Override
	public Response toResponse(Exception ex) {
		ErrorMessage errorMessage = new ErrorMessage(ex.getMessage(), 500, "No documentation yet.");
		return Response.status(Status.INTERNAL_SERVER_ERROR)
				.entity(errorMessage)
				.build();
	}

}
