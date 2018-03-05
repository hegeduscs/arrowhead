/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.eventhandler;

import eu.arrowhead.common.DatabaseManager;
import java.util.HashMap;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.log4j.Logger;

/**
 * This is the REST resource for the Event Handler Core System.
 */
@Path("eventhandler")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class EventHandlerResource {

  private final HashMap<String, Object> restrictionMap = new HashMap<>();
  private static final Logger log = Logger.getLogger(EventHandlerResource.class.getName());
  private static final DatabaseManager dm = DatabaseManager.getInstance();

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String getIt() {
    return "This is the Event Handler Arrowhead Core System.";
  }

}
