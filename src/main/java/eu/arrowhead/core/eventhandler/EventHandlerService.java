/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.core.eventhandler;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.Utility;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.EventFilter;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.messages.Event;
import eu.arrowhead.common.messages.PublishEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.apache.log4j.Logger;

final class EventHandlerService {

  private static final Logger log = Logger.getLogger(EventHandlerResource.class.getName());
  private static final DatabaseManager dm = DatabaseManager.getInstance();
  private static final HashMap<String, Object> restrictionMap = new HashMap<>();

  private static List<EventFilter> getMatchingEventFilters(PublishEvent pe) {
    restrictionMap.clear();
    restrictionMap.put("eventType", pe.getEvent().getType());
    List<EventFilter> filters = dm.getAll(EventFilter.class, restrictionMap);
    // Remove the filter if the event source is not in the filter's source list (every source is accepted, if the filter has no sources)
    filters.removeIf(current -> !current.getSources().isEmpty() && !current.getSources().contains(pe.getSource()));
    // Remove the filter if the event timestamp is not between the filter's startDate and endDate
    filters.removeIf(current -> current.getStartDate() != null && pe.getEvent().getTimestamp().isBefore(current.getStartDate()));
    filters.removeIf(current -> current.getEndDate() != null && pe.getEvent().getTimestamp().isAfter(current.getEndDate()));
    // Remove the filter if MatchMetadata = true and the event and filter metadata do not match perfectly
    filters.removeIf(current -> current.getMatchMetadata() && !pe.getEvent().getEventMetadata().equals(current.getFilterMetadata()));

    return filters;
  }

  private static boolean sendRequest(String url, Event event) {
    try {
      Utility.sendRequest(url, "POST", event);
    } catch (Exception e) {
      log.error("Publishing event to " + url + " failed.");
      e.printStackTrace();
      return false;
    }
    return true;
  }

  static Map<String, Boolean> propagateEvent(PublishEvent eventPublished) {
    // Get the event relevant filters from the DB
    List<EventFilter> filters = getMatchingEventFilters(eventPublished);
    // Create the URLs from the filters
    List<String> urls = new ArrayList<>();
    for (EventFilter filter : filters) {
      String url;
      try {
        boolean isSecure = filter.getConsumer().getAuthenticationInfo() != null;
        url = Utility.getUri(filter.getConsumer().getAddress(), filter.getPort(), filter.getNotifyUri(), isSecure, false);
      } catch (ArrowheadException | NullPointerException e) {
        e.printStackTrace();
        continue;
      }
      urls.add(url);
    }

    Map<String, Boolean> result = new ConcurrentHashMap<>();
    /*Note: ForkJoinPool.commonPool (used by CompletableFuture as default) has a default size equal to one less than the number of cores of your CPU.
      For faster execution (when there is a large number of urls), use custom thread pool manager:
      Executor myThreadPool = Executors.newFixedThreadPool(numberOfThreads);*/
    Stream<CompletableFuture> stream = urls.stream().map(url -> CompletableFuture.supplyAsync(() -> sendRequest(url, eventPublished.getEvent()))
                                                                                 .thenAcceptAsync(published -> result.put(url, published)));
    CompletableFuture.allOf(stream.toArray(CompletableFuture[]::new)).join();
    log.info("Event published to " + urls.size() + " subscribers.");
    return result;
  }

  static EventFilter saveEventFilter(EventFilter filter) {
    restrictionMap.clear();
    restrictionMap.put("systemName", filter.getConsumer().getSystemName());
    ArrowheadSystem consumer = dm.get(ArrowheadSystem.class, restrictionMap);
    if (consumer == null) {
      log.info("Consumer System " + filter.getConsumer().getSystemName() + " was not in the database, saving it now.");
      consumer = dm.save(filter.getConsumer());
    }

    restrictionMap.clear();
    restrictionMap.put("eventType", filter.getEventType());
    restrictionMap.put("consumer", consumer);
    EventFilter retrievedFilter = dm.get(EventFilter.class, restrictionMap);
    if (retrievedFilter == null) {
      filter.setConsumer(consumer);

      List<ArrowheadSystem> sources = new ArrayList<>();
      for (ArrowheadSystem source : filter.getSources()) {
        restrictionMap.clear();
        restrictionMap.put("systemName", source.getSystemName());
        ArrowheadSystem retrievedSource = dm.get(ArrowheadSystem.class, restrictionMap);
        if (retrievedSource == null) {
          retrievedSource = dm.save(source);
        }
        sources.add(retrievedSource);
      }

      filter.setSources(sources);
      return dm.save(filter);
    }

    return null;
  }

  static int deleteEventFilter(String eventType, String consumerName) {
    restrictionMap.clear();
    restrictionMap.put("systemName", consumerName);
    ArrowheadSystem consumer = dm.get(ArrowheadSystem.class, restrictionMap);
    if (consumer == null) {
      return 204; //NO CONTENT ~ call had no effect
    }

    restrictionMap.clear();
    restrictionMap.put("eventType", eventType);
    restrictionMap.put("consumer", consumer);
    EventFilter filter = dm.get(EventFilter.class, restrictionMap);
    if (filter == null) {
      return 204;
    } else {
      dm.delete(filter);
      return 200; //OK ~ delete was successful
    }
  }

}
