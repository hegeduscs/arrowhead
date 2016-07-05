package eu.arrowhead.common.exception;

import javax.ws.rs.NotAllowedException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class BadMethodExceptionMapper implements ExceptionMapper<NotAllowedException> {

	public Response toResponse(NotAllowedException exception) {

		return Response.status(Response.Status.NOT_FOUND)
				.entity(new ErrorMessage("Bad request: requested method is not allowed.", 405, 
						"No documentation yet."))
				.build();
	}
}
