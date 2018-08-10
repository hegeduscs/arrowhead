/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.core.systemregistry;

import eu.arrowhead.core.systemregistry.model.AHSystem;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.log4j.Logger;

@Path("systemregistry")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SystemRegistryResource {

	private static final Logger log = Logger.getLogger(SystemRegistryResource.class.getName());

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getIt() {
		return "This is the System Registry Arrowhead Core System.";
	}

	@GET
	@Path("lookup")
	public Response Lookup(@QueryParam("id") String id) throws Exception {
		//SystemRegistryHAAASALMySql sr = new SystemRegistryHAAASALMySql();
		SystemRegistryService srs = new SystemRegistryService();
		List<AHSystem> systems = srs.Lookup(id);
		GenericEntity entity = new GenericEntity<List<AHSystem>>(systems) {
		};

		return Response.status(200).entity(entity).build();
	}

	@POST
	@Path("publish")
	public Response Publish(@QueryParam("id") String id) throws Exception {
		Status status = null;
		String message = "";

		if (id == null) {
			return Response.status(400).entity("Please provide an ID!").build();
		}

		//SystemRegistryHAAASALMySql sr = new SystemRegistryHAAASALMySql();
		SystemRegistryService srs = new SystemRegistryService();
		
		status = srs.Publish(id);

		switch (status) {
		case CREATED:
			// system was successfully published
			message = "The System with the ID: '" + id + "' has been successfully published!";
			break;
		case CONFLICT:
			// ID already exists
			message = "The System-ID: '" + id
					+ "' has already been published! Please unpublish the system and try publishing it again!";
			break;
		default:
			message = "";
			break;
		}

		return Response.status(status).entity(message).build();
	}

	@POST
	@Path("unpublish")
	public Response Unpublish(@QueryParam("id") String id) throws Exception {
		Status status = null;
		String message = "";

		if (id == null) {
			return Response.status(400).entity("Please provide an ID!").build();
		}

		//SystemRegistryHAAASALMySql sr = new SystemRegistryHAAASALMySql();
		SystemRegistryService srs = new SystemRegistryService();
		
		status = srs.Unpublish(id);

		switch (status) {
		case OK:
			// system was successfully unpublished
			message = "The System with the ID: '" + id + "' has been successfully unpublished!";
			break;
		case NOT_FOUND:
			// ID already exists
			message = "The System-ID: '" + id + "' could not be found in the SystemRegistry! System does not exist!";
			break;
		default:
			message = "";
			break;
		}
		
		return Response.status(status).entity(message).build();
	}
}