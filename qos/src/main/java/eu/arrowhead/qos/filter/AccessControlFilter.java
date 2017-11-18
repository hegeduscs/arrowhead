package eu.arrowhead.qos.filter;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHORIZATION) //2nd highest priority constant, this filter gets executed after the SecurityFilter
public class AccessControlFilter implements ContainerRequestFilter {

  @Override
  public void filter(ContainerRequestContext requestContext) {
    //placeholder
  }

}
