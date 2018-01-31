/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.common.security;

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
