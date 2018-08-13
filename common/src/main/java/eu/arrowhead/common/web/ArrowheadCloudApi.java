package eu.arrowhead.common.web;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.database.ArrowheadCloud;
import eu.arrowhead.common.exception.DataNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.log4j.Logger;

@Path("mgmt/clouds")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ArrowheadCloudApi {

  private final HashMap<String, Object> restrictionMap = new HashMap<>();
  private static final Logger log = Logger.getLogger(ArrowheadCloudApi.class.getName());
  private static final DatabaseManager dm = DatabaseManager.getInstance();

  @GET
  public List<ArrowheadCloud> getClouds(@QueryParam("operator") String operator, @QueryParam("cloudName") String cloudName) {
    if (operator != null) {
      restrictionMap.put("operator", operator);
    }
    if (cloudName != null) {
      restrictionMap.put("cloudName", cloudName);
    }

    List<ArrowheadCloud> cloudList = dm.getAll(ArrowheadCloud.class, restrictionMap);
    if (cloudList.isEmpty()) {
      log.info("CommonApi:getAllClouds throws DataNotFoundException");
      throw new DataNotFoundException("ArrowheadClouds not found in the database.");
    }

    return cloudList;
  }

  @GET
  @Path("{cloudId}")
  public ArrowheadCloud getCloud(@PathParam("cloudId") long cloudId) {
    return dm.get(ArrowheadCloud.class, cloudId).orElseThrow(() -> new DataNotFoundException("ArrowheadCloud entry not found with id: " + cloudId));
  }

  /**
   * Adds a list of ArrowheadClouds to the database. Elements which would cause DuplicateEntryException are being skipped. The returned list only
   * contains the elements which were saved in the process.
   *
   * @return List<ArrowheadCloud>
   */
  @POST
  public Response addClouds(@Valid List<ArrowheadCloud> cloudList) {

    List<ArrowheadCloud> savedClouds = new ArrayList<>();
    for (ArrowheadCloud cloud : cloudList) {
      restrictionMap.clear();
      restrictionMap.put("operator", cloud.getOperator());
      restrictionMap.put("cloudName", cloud.getCloudName());
      ArrowheadCloud retrievedCloud = dm.get(ArrowheadCloud.class, restrictionMap);
      if (retrievedCloud == null) {
        dm.save(cloud);
        savedClouds.add(cloud);
      }
    }

    if (savedClouds.isEmpty()) {
      return Response.status(Status.NO_CONTENT).build();
    } else {
      return Response.status(Status.CREATED).entity(savedClouds).build();
    }
  }

  /**
   * Updates an existing ArrowheadCloud in the database. Returns 204 (no content) if the specified ArrowheadCloud was not in the database.
   */
  @PUT
  public Response updateCloud(@Valid ArrowheadCloud cloud) {
    restrictionMap.put("operator", cloud.getOperator());
    restrictionMap.put("cloudName", cloud.getCloudName());

    ArrowheadCloud retrievedCloud = dm.get(ArrowheadCloud.class, restrictionMap);
    if (retrievedCloud != null) {
      retrievedCloud.setAddress(cloud.getAddress());
      retrievedCloud.setPort(cloud.getPort());
      retrievedCloud.setGatekeeperServiceURI(cloud.getGatekeeperServiceURI());
      retrievedCloud.setAuthenticationInfo(cloud.getAuthenticationInfo());
      retrievedCloud.setSecure(cloud.isSecure());
      retrievedCloud = dm.merge(retrievedCloud);
      return Response.status(Status.ACCEPTED).entity(retrievedCloud).build();
    } else {
      return Response.noContent().build();
    }
  }

  @PUT
  @Path("{cloudId}")
  public Response updateCloud(@PathParam("cloudId") long cloudId, @Valid ArrowheadCloud cloud) {
    ArrowheadCloud retrievedCloud = dm.get(ArrowheadCloud.class, cloudId)
                                      .orElseThrow(() -> new DataNotFoundException("ArrowheadCloud entry not found with id: " + cloudId));
    retrievedCloud.partialUpdate(cloud);
    retrievedCloud = dm.merge(retrievedCloud);
    return Response.status(Status.ACCEPTED).entity(retrievedCloud).build();
  }

  @DELETE
  @Path("{cloudId}")
  public Response deleteCloud(@PathParam("cloudId") long cloudId) {
    return dm.get(ArrowheadCloud.class, cloudId).map(cloud -> {
      dm.delete(cloud);
      log.info("deleteCloud successfully returns.");
      return Response.ok().build();
    }).<DataNotFoundException>orElseThrow(() -> {
      log.info("deleteCloud had no effect.");
      throw new DataNotFoundException("ArrowheadCloud entry not found with id: " + cloudId);
    });
  }

}
