package eu.arrowhead.common.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class AuthenticationExceptionMapper implements ExceptionMapper<AuthenticationException>{

	@Override
	public Response toResponse(AuthenticationException ex) {
		ErrorMessage errorMessage = new ErrorMessage(ex.getMessage(), 401, "No documentation yet.");
		return Response.status(Status.UNAUTHORIZED)
				.entity(errorMessage)
				.build();
	}

}
