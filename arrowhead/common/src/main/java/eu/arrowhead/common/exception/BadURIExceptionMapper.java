package eu.arrowhead.common.exception;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class BadURIExceptionMapper implements ExceptionMapper<NotFoundException> {

	public Response toResponse(NotFoundException exception) {
		exception.printStackTrace();
		return Response.status(Response.Status.NOT_FOUND)
				.entity(new ErrorMessage("Bad request: requested URI does not exist.", 404))
				.build();
	}
}
