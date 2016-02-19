package eu.arrowhead.common.filter;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;

@Provider
public class LoggingResponseFilter implements ContainerResponseFilter {

	private static Logger log = Logger.getLogger("Outgoing");

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		log.debug("OUT." + responseContext.getStatus());

	}

}
