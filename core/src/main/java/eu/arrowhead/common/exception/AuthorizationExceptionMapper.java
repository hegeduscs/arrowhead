package eu.arrowhead.common.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class AuthorizationExceptionMapper implements ExceptionMapper<AuthorizationException>{

	@Override
	public Response toResponse(AuthorizationException ex) {
		ErrorMessage errorMessage = new ErrorMessage(ex.getMessage(), 401, "No documentation yet.");
		return Response.status(Status.UNAUTHORIZED)
				.entity(errorMessage)
				.build();
	}

}
