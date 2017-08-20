package eu.arrowhead.core.api;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.database.CoreSystem;
import eu.arrowhead.common.database.NeighborCloud;
import eu.arrowhead.common.database.OwnCloud;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.model.ArrowheadCloud;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.log4j.Logger;

@Path("configuration")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConfigurationApi {

  private static Logger log = Logger.getLogger(ConfigurationApi.class.getName());
  private DatabaseManager dm = DatabaseManager.getInstance();
  private HashMap<String, Object> restrictionMap = new HashMap<>();

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String getIt() {
    return "Got it";
  }

  /**
   * Returns all the Core Systems from the database.
   *
   * @return List<CoreSystem>
   */
  @GET
  @Path("/coresystems")
  public List<CoreSystem> getAllCoreSystems() {

    List<CoreSystem> systemList = new ArrayList<>();
    systemList = dm.getAll(CoreSystem.class, restrictionMap);
    if (systemList.isEmpty()) {
      log.info("ConfigurationApi:getAllCoreSystems throws DataNotFoundException");
      throw new DataNotFoundException("CoreSystems not found in the database.");
    }

    return systemList;
  }

  /**
   * Returns all the Neighbor Clouds from the database.
   *
   * @return List<NeighborCloud>
   */
  @GET
  @Path("/neighborhood")
  public List<NeighborCloud> getAllNeighborClouds() {

    List<NeighborCloud> cloudList = new ArrayList<>();
    cloudList = dm.getAll(NeighborCloud.class, restrictionMap);
    if (cloudList.isEmpty()) {
      log.info("ConfigurationApi:getAllNeighborClouds throws DataNotFoundException");
      throw new DataNotFoundException("NeighborClouds not found in the database.");
    }

    return cloudList;
  }

  /**
   * Returns all the Own Clouds from the database.
   *
   * @return List<OwnCloud>
   */
  @GET
  @Path("/owncloud")
  public List<OwnCloud> getAllOwnClouds() {

    List<OwnCloud> cloudList = new ArrayList<>();
    cloudList = dm.getAll(OwnCloud.class, restrictionMap);
    if (cloudList.isEmpty()) {
      log.info("ConfigurationApi:getAllOwnClouds throws DataNotFoundException");
      throw new DataNotFoundException("OwnClouds not found in the database.");
    }

    return cloudList;
  }

  /**
   * Returns a Core System from the database specified by the system name.
   *
   * @return CoreSystem
   */
  @GET
  @Path("/coresystems/{systemName}")
  public Response getCoreSystem(@PathParam("systemName") String systemName) {

    restrictionMap.put("systemName", systemName);
    CoreSystem coreSystem = dm.get(CoreSystem.class, restrictionMap);
    if (coreSystem == null) {
      log.info("ConfigurationApi:getCoreSystem throws DataNotFoundException");
      throw new DataNotFoundException("Requested CoreSystem not found in the database.");
    }

    return Response.status(Status.OK).entity(coreSystem).build();
  }

  /**
   * Returns a Neighbor Cloud from the database specified by the operator and cloud name.
   *
   * @return NeighborCloud
   */
  @GET
  @Path("/neighborhood/operator/{operator}/cloudname/{cloudName}")
  public Response getNeighborCloud(@PathParam("operator") String operator, @PathParam("cloudName") String cloudName) {

    restrictionMap.put("operator", operator);
    restrictionMap.put("cloudName", cloudName);
    ArrowheadCloud cloud = dm.get(ArrowheadCloud.class, restrictionMap);
    if (cloud == null) {
      log.info("ConfigurationApi:getNeighborCloud throws DataNotFoundException");
      throw new DataNotFoundException("Requested NeighborCloud not found in the database.");
    }

    restrictionMap.clear();
    restrictionMap.put("cloud", cloud);
    NeighborCloud neighborCloud = dm.get(NeighborCloud.class, restrictionMap);
    if (neighborCloud == null) {
      log.info("ConfigurationApi:getNeighborCloud throws DataNotFoundException");
      throw new DataNotFoundException("Requested NeighborCloud not found in the database.");
    }

    return Response.status(Status.OK).entity(neighborCloud).build();
  }

  /**
   * Adds a list of CoreSystems to the database. Elements which would cause DuplicateEntryException or BadPayloadException (caused by missing
   * systemName, address or serviceURI) are being skipped. The returned list only contains the elements which was saved in the process.
   *
   * @return List<CoreSystem>
   */
  @POST
  @Path("/coresystems")
  public List<CoreSystem> addCoreSystems(List<CoreSystem> coreSystemList) {

    List<CoreSystem> savedCoreSystems = new ArrayList<>();
    for (CoreSystem cs : coreSystemList) {
      if (cs.isValid()) {
        restrictionMap.clear();
        restrictionMap.put("systemName", cs.getSystemName());
        CoreSystem retrievedCoreSystem = dm.get(CoreSystem.class, restrictionMap);
        if (retrievedCoreSystem == null) {
          dm.save(cs);
          savedCoreSystems.add(cs);
        }
      }
    }

    return savedCoreSystems;
  }

  /**
   * Adds a list of NeighborClouds to the database. Elements which would cause DuplicateEntryException or BadPayloadException (caused by missing
   * operator, cloudName, address or serviceURI) are being skipped. The returned list only contains the elements which was saved in the process.
   *
   * @return List<NeighborCloud>
   */
  @POST
  @Path("/neighborhood")
  public List<NeighborCloud> addNeighborClouds(List<NeighborCloud> neighborCloudList) {

    List<NeighborCloud> savedNeighborClouds = new ArrayList<>();
    for (NeighborCloud nc : neighborCloudList) {
      if (nc.isValid()) {
        restrictionMap.clear();
        restrictionMap.put("operator", nc.getCloud().getOperator());
        restrictionMap.put("cloudName", nc.getCloud().getCloudName());
        ArrowheadCloud cloud = dm.get(ArrowheadCloud.class, restrictionMap);
        if (cloud == null) {
          dm.save(nc.getCloud());
        } else {
          nc.setCloud(cloud);
        }

        restrictionMap.clear();
        restrictionMap.put("cloud", cloud);
        NeighborCloud neighborCloud = dm.get(NeighborCloud.class, restrictionMap);
        if (neighborCloud == null) {
          dm.merge(nc);
          savedNeighborClouds.add(nc);
        }
      }
    }

    return savedNeighborClouds;
  }

  /**
   * Adds an instance of OwnCloud to the database. Deletes any other row in this table, before doing this save. Missing operator, cloudName or address
   * causes BadPayloadException!
   *
   * @return OwnCloud
   */
  @POST
  @Path("/owncloud")
  public OwnCloud addOwnCloud(OwnCloud ownCloud) {

    if (!ownCloud.isValid()) {
      log.info("ConfigurationApi:addOwnCloud throws BadPayloadException");
      throw new BadPayloadException("Bad payload: missing operator, cloudName " + "or address field! (ConfigurationApi:addOwnCloud)");
    }

    List<OwnCloud> ownClouds = new ArrayList<>();
    ownClouds = dm.getAll(OwnCloud.class, restrictionMap);
    if (!ownClouds.isEmpty()) {
      for (OwnCloud cloud : ownClouds) {
        dm.delete(cloud);
      }
    }

    ownCloud = dm.save(ownCloud);
    return ownCloud;
  }

  /**
   * Updates an existing CoreSystem in the database. Returns 204 (no content) if the specified CoreSystem was not in the database.
   */
  @PUT
  @Path("/coresystems")
  public Response updateCoreSystem(CoreSystem cs) {

    if (!cs.isValid()) {
      log.info("ConfigurationApi:updateCoreSystem throws BadPayloadException");
      throw new BadPayloadException("Bad payload: missing systemName, address or " + "serviceURI in the entry payload.");
    }

    restrictionMap.put("systemName", cs.getSystemName());
    CoreSystem coreSystem = dm.get(CoreSystem.class, restrictionMap);
    if (coreSystem != null) {
      coreSystem.setAddress(cs.getAddress());
      coreSystem.setPort(cs.getPort());
      coreSystem.setAuthenticationInfo(cs.getAuthenticationInfo());
      coreSystem.setServiceURI(cs.getServiceURI());

      coreSystem = dm.merge(coreSystem);
      return Response.status(Status.ACCEPTED).entity(coreSystem).build();
    } else {
      return Response.noContent().build();
    }

  }

  /**
   * Updates an existing NeighborCloud in the database. Returns 204 (no content) if the specified NeighborCloud was not in the database.
   */
  @PUT
  @Path("/neighborhood")
  public Response updateNeighborCloud(NeighborCloud nc) {

    if (!nc.isValid()) {
      log.info("ConfigurationApi:updateNeighborCloud throws BadPayloadException");
      throw new BadPayloadException("Bad payload: missing/incomplete arrowheadcloud" + "in the entry payload.");
    }

    restrictionMap.put("operator", nc.getCloud().getOperator());
    restrictionMap.put("cloudName", nc.getCloud().getCloudName());
    ArrowheadCloud cloud = dm.get(ArrowheadCloud.class, restrictionMap);

    restrictionMap.clear();
    restrictionMap.put("cloud", cloud);
    NeighborCloud neighborCloud = dm.get(NeighborCloud.class, restrictionMap);
    if (neighborCloud != null) {
      neighborCloud.getCloud().setAddress(nc.getCloud().getAddress());
      neighborCloud.getCloud().setPort(nc.getCloud().getPort());
      neighborCloud.getCloud().setAuthenticationInfo(nc.getCloud().getAuthenticationInfo());
      neighborCloud.getCloud().setGatekeeperServiceURI(nc.getCloud().getGatekeeperServiceURI());

      neighborCloud = dm.merge(neighborCloud);
      return Response.status(Status.ACCEPTED).entity(neighborCloud).build();
    } else {
      return Response.noContent().build();
    }

  }

  /**
   * Deletes the CoreSystem from the database specified by the system name. Returns 200 if the delete is succesful, 204 (no content) if the system was
   * not in the database to begin with.
   */
  @DELETE
  @Path("/coresystems/{systemName}")
  public Response deleteCoreSystem(@PathParam("systemName") String systemName) {

    restrictionMap.put("systemName", systemName);
    CoreSystem retrievedSystem = dm.get(CoreSystem.class, restrictionMap);
    if (retrievedSystem == null) {
      return Response.noContent().build();
    } else {
      dm.delete(retrievedSystem);
      return Response.ok().build();
    }
  }

  /**
   * Deletes the NeighborCloud from the database specified by the operator and cloud name. Returns 200 if the delete is succesful, 204 (no content) if
   * the system was not in the database to begin with.
   */
  @DELETE
  @Path("/neighborhood/operator/{operator}/cloudname/{cloudName}")
  public Response deleteNeighborCloud(@PathParam("operator") String operator, @PathParam("cloudName") String cloudName) {

    restrictionMap.put("operator", operator);
    restrictionMap.put("cloudName", cloudName);
    ArrowheadCloud cloud = dm.get(ArrowheadCloud.class, restrictionMap);
    if (cloud == null) {
      return Response.noContent().build();
    }

    restrictionMap.clear();
    restrictionMap.put("cloud", cloud);
    NeighborCloud neighborCloud = dm.get(NeighborCloud.class, restrictionMap);
    if (neighborCloud == null) {
      return Response.noContent().build();
    } else {
      dm.delete(neighborCloud);
      return Response.ok().build();
    }
  }


}
