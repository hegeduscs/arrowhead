package eu.arrowhead.common.filter;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;

@Provider
public class LoggingRequestFilter implements ContainerRequestFilter {

	private static Logger log = Logger.getLogger("Incoming");

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		log.debug("IN." + requestContext.getMethod() + ": " + requestContext.getUriInfo().getPath());

	}

}
