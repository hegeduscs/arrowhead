/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.core.deviceregistry;

import eu.arrowhead.common.deviceregistry.AHDevice;
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

@Path("deviceregistry")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DeviceRegistryResource {

  private static final Logger log = Logger.getLogger(DeviceRegistryResource.class.getName());

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String getIt() {
    return "This is the Device Registry Arrowhead Core System.";
  }
	
	@GET
	@Path("lookup")
	public Response Lookup(@QueryParam("id") String id) throws Exception{
		DeviceRegistryService drs = new DeviceRegistryService();
		List<AHDevice> devices = drs.Lookup(id);
		GenericEntity entity = new GenericEntity<List<AHDevice>>(devices) {};
		
		return Response.status(200).entity(entity).build();
	}
	
	@POST
	@Path("publish") 
	public Response Publish(@QueryParam("id") String id) throws Exception{
		Status status = null;
		String message = "";
		
		if(id == null) {
			return Response.status(400).entity("Please provide an ID!").build();
		}
		
		DeviceRegistryService drs = new DeviceRegistryService();
		
		status = drs.Publish(id);
		
		switch(status) {
		case CREATED:
			//system was successfully published
			message = "The Device with the ID: '" + id + "' has been successfully published!";
			break;
		case CONFLICT:
			//ID already exists
			message = "The Device-ID: '" + id + "' has already been published! Please unpublish the device and try publishing it again!";
			break;
		default:
			message = "";
			break;
		}
		
		return Response.status(status).entity(message).build();
	}
	
	@POST
	@Path("unpublish") 
	public Response Unpublish(@QueryParam("id") String id) throws Exception{
		Status status = null;
		String message = "";
		
		if(id == null) {
			return Response.status(400).entity("Please provide an ID!").build();
		}
		
		DeviceRegistryService drs = new DeviceRegistryService();
		
		status = drs.Unpublish(id);
		
		switch(status) {
		case OK:
			//system was successfully unpublished
			message = "The Device with the ID: '" + id + "' has been successfully unpublished!";
			break;
		case NOT_FOUND:
			//ID already exists
			message = "The Device-ID: '" + id + "' could not be found in the DeviceRegistry! Device does not exist!";
			break;
		default:
			message = "";
			break;
		}
		
		return Response.status(status).entity(message).build();
	}
}
