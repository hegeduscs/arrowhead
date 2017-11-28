package eu.arrowhead.core.orchestrator.filter;

import eu.arrowhead.common.Utility;
import eu.arrowhead.core.orchestrator.OrchestratorMain;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import org.jetbrains.annotations.NotNull;

@Provider
@Priority(Priorities.USER)
public class InboundDebugFilter implements ContainerRequestFilter {

  @Override
  public void filter(@NotNull ContainerRequestContext requestContext) throws IOException {
    if (OrchestratorMain.DEBUG_MODE) {
      System.out.println("New " + requestContext.getMethod() + " request at: " + requestContext.getUriInfo().getRequestUri().toString());
      BufferedReader br = new BufferedReader(new InputStreamReader(requestContext.getEntityStream(), "utf-8"));
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line).append("\n");
      }
      br.close();

      if (!sb.toString().isEmpty()) {
        String prettyJson = Utility.toPrettyJson(sb.toString(), null);
        System.out.println(prettyJson);
        InputStream in = new ByteArrayInputStream(prettyJson.getBytes("UTF-8"));
        requestContext.setEntityStream(in);
      }
    }
  }
}
