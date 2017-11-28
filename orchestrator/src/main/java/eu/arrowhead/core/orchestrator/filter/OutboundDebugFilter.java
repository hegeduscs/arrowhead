package eu.arrowhead.core.orchestrator.filter;

import eu.arrowhead.common.Utility;
import eu.arrowhead.core.orchestrator.OrchestratorMain;
import java.io.IOException;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import org.jetbrains.annotations.NotNull;

@Provider
@Priority(Priorities.USER)
public class OutboundDebugFilter implements ContainerResponseFilter {

  @Override
  public void filter(@NotNull ContainerRequestContext requestContext, @NotNull ContainerResponseContext responseContext) throws IOException {
    if (OrchestratorMain.DEBUG_MODE) {
      if (responseContext.getEntity() != null) {
        System.out.println("Response to the request at: " + requestContext.getUriInfo().getRequestUri().toString());
        System.out.println(Utility.toPrettyJson(null, responseContext.getEntity()));
      }
    }
  }
}
