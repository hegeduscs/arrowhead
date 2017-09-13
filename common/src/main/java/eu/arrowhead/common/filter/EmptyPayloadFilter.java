package eu.arrowhead.common.filter;

import eu.arrowhead.common.exception.ErrorMessage;
import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class EmptyPayloadFilter implements ContainerRequestFilter {

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    String method = requestContext.getMethod();
    if (method.equals("POST") || method.equals("PUT")) {
      int contentLength = requestContext.getLength();
      if (contentLength == 0) {
        ErrorMessage em = new ErrorMessage(
            "Payload is null (unusual for POST/PUT request)! If you want to send an empty payload, try sending empty brackets ({})", 400,
            RuntimeException.class.toString());
        requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST).entity(em).build());
      }
    }
  }
}
