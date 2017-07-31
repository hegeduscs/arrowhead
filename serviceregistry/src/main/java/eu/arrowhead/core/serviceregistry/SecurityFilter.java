package eu.arrowhead.core.serviceregistry;

import java.io.IOException;
import java.security.Principal;
import java.security.cert.X509Certificate;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

class SecurityFilter implements ContainerRequestFilter {

  private static final String REALM = "HTTPS Example authentication";
  @Inject
  private javax.inject.Provider<UriInfo> uriInfo;

  @Override
  public void filter(ContainerRequestContext context) throws IOException {

    X509Certificate[] chain = (X509Certificate[]) context.getProperty("javax.servlet.request.X509Certificate");

    System.out.println("Filter called, request contains " + chain.length + " certificates");
    if (chain != null && chain.length > 0) {
      String subject = chain[0].getSubjectDN().getName();
      System.out.println("Subject field: " + subject);
      Authorizer securityContext = new Authorizer(subject);
      context.setSecurityContext(securityContext);
    }

  }

  public class Authorizer implements SecurityContext {

    private String user;
    private Principal principal;

    public Authorizer(final String user) {
      this.user = user;
      this.principal = new Principal() {

        public String getName() {
          return user;
        }
      };
    }

    public Principal getUserPrincipal() {
      return this.principal;
    }

    public boolean isUserInRole(String role) {
      return (role.equals(user));
    }

    public boolean isSecure() {
      return "https".equals(uriInfo.get().getRequestUri().getScheme());
    }

    public String getAuthenticationScheme() {
      return SecurityContext.BASIC_AUTH;
    }
  }

}


