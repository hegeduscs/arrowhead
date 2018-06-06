/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.filter;

import eu.arrowhead.common.Utility;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
    if (Boolean.valueOf(System.getProperty("debug_mode", "false"))) {
      System.out.println("New " + requestContext.getMethod() + " request at: " + requestContext.getUriInfo().getRequestUri().toString());
      String prettyJson = Utility.getRequestPayload(requestContext.getEntityStream());
      System.out.println(prettyJson);

      InputStream in = new ByteArrayInputStream(prettyJson.getBytes("UTF-8"));
      requestContext.setEntityStream(in);
    }
  }
}
