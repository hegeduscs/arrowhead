package eu.arrowhead.common.exception;

import javax.ws.rs.NotAllowedException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class BadMethodExceptionMapper implements ExceptionMapper<NotAllowedException> {

	public Response toResponse(NotAllowedException ex) {
		ex.printStackTrace();
		ErrorMessage errorMessage = null;
		if(ex.getMessage() != null){
			errorMessage = new ErrorMessage(ex.getMessage(), 405);
		}
		else{
			errorMessage = new ErrorMessage("Bad request: requested method is not allowed.", 405);
		}
		
		return Response.status(Response.Status.METHOD_NOT_ALLOWED)
				.entity(errorMessage)
				.build();
	}
}
