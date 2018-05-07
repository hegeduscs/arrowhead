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
import eu.arrowhead.common.database.EventFilter;
import eu.arrowhead.common.messages.PublishEvent;
import java.util.Collections;
import java.util.HashMap;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
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

  @POST
  @Path("publish")
  public Response publishEvent(PublishEvent event) {
    event.missingFields(true, null);

    //start async process

    //return OK

    /*ExecutorService executorService = Executors.newFixedThreadPool(20);
    CompletableFuture.supplyAsync(() -> MyFileService.resize(myfile), executorService)*/

    /*  async process steps:
        1) query DB for event type
        2) filter out filters where the source is not in the sourcelist
        3) more filtering based on the dates + metadata alapján
        4) for the rest, put together the notifying URL and send request to them
        5) if all finished, optionally send a deliveryComplete ack to source

        getMatchingEventFilters
        getSubscriberUrls
        Task with sendRequest()
     */

    /*ExecutorService executor = Executors.newWorkStealingPool();

    List<Callable<String>> callables = Arrays.asList(
        () -> "task1",
        () -> "task2",
        () -> "task3");

    executor.invokeAll(callables)
            .stream()
            .map(future -> {
              try {
                return future.get();
              }
              catch (Exception e) {
                throw new IllegalStateException(e);
              }
            })
            .forEach(System.out::println);*/

    //TODO kéne majd valami számon tartja a pozitív/negatív válaszok számát a notifyokra, synchronized változó valahol pl, ami a publishEventtel
    // jön létre, és lelogoljuk az értékét, illetve a deliverycompletba is visszaadhatjuk akár

    /*try {
      System.out.println("attempt to shutdown executor");
      executor.shutdown();
      executor.awaitTermination(5, TimeUnit.SECONDS);
    }
    catch (InterruptedException e) {
      System.err.println("tasks interrupted");
    }
    finally {
      if (!executor.isTerminated()) {
        System.err.println("cancel non-finished tasks");
      }
      executor.shutdownNow();
      System.out.println("shutdown finished");
    }*/
    return null;
  }

  //TODO legyen egy Task, ami azokat a filtereket törli, aminek a stopDate-je is a múltban van már, opcionális működés mint SR-nél
  // + publish eventnél mennyivel régibb timestamp legyen még megengedhető

  @POST
  @Path("subscription")
  public Response subscribe(EventFilter filter) {
    filter.missingFields(true, Collections.singleton("ArrowheadSystem:port"));
    filter.toDatabase();
    EventFilter savedFilter = EventHandlerService.saveEventFilter(filter);
    if (savedFilter != null) {
      log.info("EventFilter was saved.");
      savedFilter.fromDatabase();
      return Response.status(Status.CREATED.getStatusCode()).entity(savedFilter).build();
    } else {
      log.info("EventFilter was already in the database, nothing happened.");
      return Response.status(Status.NO_CONTENT.getStatusCode()).build();
    }
  }

  @DELETE
  @Path("subscription/type/{eventType}/consumer/{consumerName}")
  public Response unsubscribe(@PathParam("eventType") String eventType, @PathParam("consumerName") String consumerName) {
    int statusCode = EventHandlerService.deleteEventFilter(eventType, consumerName);
    log.info("deleteEventFilter returned with status code: " + statusCode);
    return Response.status(statusCode).build();
  }

}
