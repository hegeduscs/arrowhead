/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.core.gatekeeper;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.database.ArrowheadCloud;
import eu.arrowhead.common.database.Broker;
import eu.arrowhead.common.database.NeighborCloud;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.log4j.Logger;

@Path("gatekeeper/mgmt")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GatekeeperApi {

  private final HashMap<String, Object> restrictionMap = new HashMap<>();
  private static final Logger log = Logger.getLogger(GatekeeperApi.class.getName());
  private static final DatabaseManager dm = DatabaseManager.getInstance();

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String getIt() {
    return "gatekeeper/mgmt got it";
  }

  /**
   * Returns all the Neighbor Clouds from the database.
   *
   * @return List<NeighborCloud>
   */
  @GET
  @Path("neighborhood")
  public List<NeighborCloud> getAllNeighborClouds() {

    List<NeighborCloud> cloudList = dm.getAll(NeighborCloud.class, restrictionMap);
    if (cloudList.isEmpty()) {
      log.info("GatekeeperApi:getAllNeighborClouds throws DataNotFoundException");
      throw new DataNotFoundException("NeighborClouds not found in the database.");
    }

    return cloudList;
  }

  /**
   * Returns a Neighbor Cloud from the database specified by the operator and cloud name.
   *
   * @return NeighborCloud
   */
  @GET
  @Path("neighborhood/operator/{operator}/cloudname/{cloudName}")
  public Response getNeighborCloud(@PathParam("operator") String operator, @PathParam("cloudName") String cloudName) {

    restrictionMap.put("operator", operator);
    restrictionMap.put("cloudName", cloudName);
    ArrowheadCloud cloud = dm.get(ArrowheadCloud.class, restrictionMap);
    if (cloud == null) {
      log.info("GatekeeperApi:getNeighborCloud throws DataNotFoundException");
      throw new DataNotFoundException("Requested NeighborCloud not found in the database.");
    }

    restrictionMap.clear();
    restrictionMap.put("cloud", cloud);
    NeighborCloud neighborCloud = dm.get(NeighborCloud.class, restrictionMap);
    if (neighborCloud == null) {
      log.info("GatekeeperApi:getNeighborCloud throws DataNotFoundException");
      throw new DataNotFoundException("Requested NeighborCloud not found in the database.");
    }

    return Response.status(Status.OK).entity(neighborCloud).build();
  }

  /**
   * Adds a list of NeighborClouds to the database. Elements which would cause DuplicateEntryException or BadPayloadException (caused by missing
   * operator, cloudName, address or serviceURI) are being skipped. The returned list only contains the elements which were saved in the process.
   *
   * @return List<NeighborCloud>
   */
  @POST
  @Path("neighborhood")
  public Response addNeighborClouds(@Valid List<NeighborCloud> neighborCloudList) {

    List<NeighborCloud> savedNeighborClouds = new ArrayList<>();
    for (NeighborCloud nc : neighborCloudList) {
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
        dm.save(nc);
        savedNeighborClouds.add(nc);
      }
    }

    if (savedNeighborClouds.isEmpty()) {
      return Response.status(Status.NO_CONTENT).build();
    } else {
      return Response.status(Status.CREATED).entity(savedNeighborClouds).build();
    }
  }

  /**
   * Updates an existing NeighborCloud in the database. Returns 204 (no content) if the specified NeighborCloud was not in the database.
   */
  @PUT
  @Path("neighborhood")
  public Response updateNeighborCloud(@Valid NeighborCloud nc) {
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
   * Deletes the NeighborCloud from the database specified by the operator and cloud name. Returns 200 if the delete is successful, 204 (no content)
   * if the cloud was not in the database to begin with.
   */
  @DELETE
  @Path("neighborhood/operator/{operator}/cloudname/{cloudName}")
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

  @GET
  @Path("brokers")
  public List<Broker> getAllBrokers() {

    List<Broker> brokerList = dm.getAll(Broker.class, restrictionMap);
    if (brokerList.isEmpty()) {
      log.info("GatekeeperApi:getAllBrokers throws DataNotFoundException");
      throw new DataNotFoundException("Brokers not found in the database.");
    }

    return brokerList;
  }

  @GET
  @Path("brokers/brokername/{brokerName}")
  public Broker getBrokerByName(@PathParam("brokerName") String brokerName) {

    restrictionMap.put("brokerName", brokerName);
    Broker broker = dm.get(Broker.class, restrictionMap);
    if (broker == null) {
      log.info("getBrokerByName throws DataNotFoundException");
      throw new DataNotFoundException("Requested Broker not found in the database.");
    }

    return broker;
  }

  @GET
  @Path("brokers/address/{address}")
  public List<Broker> getBrokerByAddress(@PathParam("address") String address) {

    restrictionMap.put("address", address);
    List<Broker> brokerList = dm.getAll(Broker.class, restrictionMap);
    if (brokerList.isEmpty()) {
      log.info("getBrokerByAddress throws DataNotFoundException");
      throw new DataNotFoundException("Brokers with this address not found in the database.");
    }

    return brokerList;
  }

  @GET
  @Path("brokers/address/{address}/port/{port}")
  public List<Broker> getBrokerByAddressAndPort(@PathParam("address") String address, @PathParam("port") int port) {

    restrictionMap.put("address", address);
    restrictionMap.put("port", port);
    List<Broker> brokerList = dm.getAll(Broker.class, restrictionMap);
    if (brokerList.isEmpty()) {
      log.info("getBrokerByAddressAndPort throws DataNotFoundException");
      throw new DataNotFoundException("Brokers with this address and port not found in the database.");
    }

    return brokerList;
  }

  @POST
  @Path("brokers")
  public Response addBrokers(@Valid List<Broker> brokerList) {

    List<Broker> savedBrokers = new ArrayList<>();
    for (Broker broker : brokerList) {
      restrictionMap.clear();
      restrictionMap.put("brokerName", broker.getBrokerName());
      Broker retrievedBroker = dm.get(Broker.class, restrictionMap);
      if (retrievedBroker == null) {
        dm.save(broker);
        savedBrokers.add(broker);
      }
    }

    if (savedBrokers.isEmpty()) {
      return Response.status(Status.NO_CONTENT).build();
    } else {
      return Response.status(Status.CREATED).entity(savedBrokers).build();
    }
  }

  @PUT
  @Path("brokers")
  public Response updateBroker(@Valid Broker broker) {
    restrictionMap.put("brokerName", broker.getBrokerName());
    Broker retrievedBroker = dm.get(Broker.class, restrictionMap);
    if (retrievedBroker != null) {
      retrievedBroker.setAddress(broker.getAddress());
      retrievedBroker.setPort(broker.getPort());
      retrievedBroker.setAuthenticationInfo(broker.getAuthenticationInfo());
      retrievedBroker.setSecure(broker.isSecure());
      retrievedBroker = dm.merge(retrievedBroker);
      return Response.status(Status.ACCEPTED).entity(retrievedBroker).build();
    } else {
      return Response.noContent().build();
    }
  }

  @DELETE
  @Path("brokers/brokername/{brokerName}")
  public Response deleteBroker(@PathParam("brokerName") String brokerName) {

    restrictionMap.put("brokerName", brokerName);
    Broker broker = dm.get(Broker.class, restrictionMap);
    if (broker == null) {
      return Response.noContent().build();
    } else {
      dm.delete(broker);
      return Response.ok().build();
    }
  }

}
