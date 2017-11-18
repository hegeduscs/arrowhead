package eu.arrowhead.core.orchestrator.filter;

import java.io.IOException;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.USER)
public class OutboundDebugFilter implements ContainerRequestFilter {

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {

  }
}
