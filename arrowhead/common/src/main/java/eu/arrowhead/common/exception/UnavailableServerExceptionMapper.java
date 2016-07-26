package eu.arrowhead.common.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class UnavailableServerExceptionMapper implements ExceptionMapper<UnavailableServerException>{

	@Override
	public Response toResponse(UnavailableServerException ex) {
		ErrorMessage errorMessage = new ErrorMessage(ex.getMessage(), 503, "No documentation yet.");
		return Response.status(Status.SERVICE_UNAVAILABLE)
				.entity(errorMessage)
				.build();
	}

}

