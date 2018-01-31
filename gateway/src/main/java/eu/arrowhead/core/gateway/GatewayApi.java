/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.gateway;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.database.Broker;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
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

@Path("gateway/mgmt")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GatewayApi {

  private static final Logger log = Logger.getLogger(GatewayApi.class.getName());
  private final DatabaseManager dm = DatabaseManager.getInstance();
  private final HashMap<String, Object> restrictionMap = new HashMap<>();


  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String getIt() {
    return "This is the Gateway API.";
  }

  @GET
  @Path("brokers")
  public List<Broker> getAllBroker() {

    List<Broker> brokerList = dm.getAll(Broker.class, restrictionMap);
    if (brokerList.isEmpty()) {
      log.info("getAllBroker throws DataNotFoundException");
      throw new DataNotFoundException("Brokers not found in the database.", Status.NOT_FOUND.getStatusCode(), DataNotFoundException.class.getName(),
                                      GatewayApi.class.toString());
    }

    return brokerList;
  }

  @GET
  @Path("brokers/brokername/{brokerName}")
  public Broker getBroker(@PathParam("brokerName") String brokerName) {

    restrictionMap.put("brokerName", brokerName);
    Broker broker = dm.get(Broker.class, restrictionMap);
    if (broker == null) {
      log.info("getBroker throws DataNotFoundException");
      throw new DataNotFoundException("Requested Broker not found in the database.", Status.NOT_FOUND.getStatusCode(), DataNotFoundException.class
          .getName(), GatewayApi.class.toString());
    }

    return broker;
  }

  @POST
  @Path("brokers")
  public Response addBrokers(List<Broker> brokerList) {

    List<Broker> savedBrokers = new ArrayList<>();
    for (Broker broker : brokerList) {
      if (broker.isValid()) {
        restrictionMap.clear();
        restrictionMap.put("brokerName", broker.getBrokerName());
        Broker retrievedBroker = dm.get(Broker.class, restrictionMap);
        if (retrievedBroker == null) {
          dm.save(broker);
          savedBrokers.add(broker);
        }
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
  public Response updateBroker(Broker broker) {

    if (!broker.isValid()) {
      log.info("updateBroker throws BadPayloadException");
      throw new BadPayloadException("Bad payload: missing broker name or address.", Status.BAD_REQUEST.getStatusCode(), BadPayloadException.class.getName(), GatewayApi.class.toString());
    }

    restrictionMap.put("brokerName", broker.getBrokerName());
    Broker retrievedBroker = dm.get(Broker.class, restrictionMap);
    if (retrievedBroker != null) {
      retrievedBroker.setAddress(broker.getAddress());
      retrievedBroker.setPort(broker.getPort());
      retrievedBroker.setSecure(broker.isSecure());
      retrievedBroker.setAuthenticationInfo(broker.getAuthenticationInfo());
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
