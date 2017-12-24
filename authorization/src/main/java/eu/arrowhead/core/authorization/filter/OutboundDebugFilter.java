package eu.arrowhead.core.authorization.filter;

import eu.arrowhead.common.Utility;
import eu.arrowhead.core.authorization.AuthorizationMain;
import java.io.IOException;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.USER)
public class OutboundDebugFilter implements ContainerResponseFilter {

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
    if (AuthorizationMain.DEBUG_MODE) {
      if (responseContext.getEntity() != null) {
        System.out.println("Response to the request at: " + requestContext.getUriInfo().getRequestUri().toString());
        System.out.println(Utility.toPrettyJson(null, responseContext.getEntity()));
      }
    }
  }
}
