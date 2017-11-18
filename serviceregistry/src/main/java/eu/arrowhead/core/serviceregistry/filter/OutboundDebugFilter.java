package eu.arrowhead.core.serviceregistry.filter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.arrowhead.core.serviceregistry.ServiceRegistryMain;
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

  private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
    if (ServiceRegistryMain.DEBUG_MODE) {
      System.out.println("Response to the request at: " + requestContext.getUriInfo().getRequestUri().toString());
      System.out.println(gson.toJson(responseContext.getEntity()));
    }
  }
}
