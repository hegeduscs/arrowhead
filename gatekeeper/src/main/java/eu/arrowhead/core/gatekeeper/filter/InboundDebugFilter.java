package eu.arrowhead.core.gatekeeper.filter;

import eu.arrowhead.core.gatekeeper.GatekeeperMain;
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

@Provider
@Priority(Priorities.USER)
public class InboundDebugFilter implements ContainerRequestFilter {

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    if (GatekeeperMain.DEBUG_MODE) {
      System.out.println("New request at: " + requestContext.getUriInfo().getRequestUri().toString());
      BufferedReader br = new BufferedReader(new InputStreamReader(requestContext.getEntityStream(), "utf-8"));
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line).append("\n");
      }
      br.close();
      System.out.println(sb.toString());
      InputStream in = new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
      requestContext.setEntityStream(in);
    }
  }
}
