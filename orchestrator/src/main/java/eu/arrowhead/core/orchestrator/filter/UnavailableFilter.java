package eu.arrowhead.core.orchestrator.filter;

import eu.arrowhead.common.Utility;
import eu.arrowhead.common.exception.UnavailableServerException;
import eu.arrowhead.core.orchestrator.OrchestratorMain;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.USER)
public class UnavailableFilter implements ContainerResponseFilter {

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    if (responseContext.getStatus() == Status.SERVICE_UNAVAILABLE.getStatusCode()) {
      String response = Utility.toPrettyJson(null, responseContext.getEntity());
      if (response.contains(UnavailableServerException.class.getName())) {
        Thread querySR = new Thread(new Runnable() {
          public void run() {
            OrchestratorMain.getCoreSystemServiceUris();
          }
        });

        querySR.start();
      }
    }
  }
}
