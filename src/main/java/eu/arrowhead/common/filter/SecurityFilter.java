/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.filter;

import java.security.Principal;
import java.security.cert.X509Certificate;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION) //Highest priority constant, this filter gets executed first
public class SecurityFilter implements ContainerRequestFilter {

  private UriInfo uriInfo;

  @Override
  public void filter(ContainerRequestContext context) {
    X509Certificate[] chain = (X509Certificate[]) context.getProperty("javax.servlet.request.X509Certificate");
    if (chain != null && chain.length > 0) {
      uriInfo = context.getUriInfo();
      String subject = chain[0].getSubjectDN().getName();
      Authorizer securityContext = new Authorizer(subject);
      context.setSecurityContext(securityContext);
    }
  }

  class Authorizer implements SecurityContext {

    private String user;
    private Principal principal;

    Authorizer(final String user) {
      this.user = user;
      this.principal = () -> user;
    }

    public Principal getUserPrincipal() {
      return this.principal;
    }

    public boolean isUserInRole(String role) {
      return (role.equals(user));
    }

    public boolean isSecure() {
      return uriInfo.getRequestUri().getScheme().equals("https");
    }

    public String getAuthenticationScheme() {
      return SecurityContext.BASIC_AUTH;
    }
  }


}
