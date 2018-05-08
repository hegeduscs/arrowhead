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
import eu.arrowhead.common.messages.PublishEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.apache.log4j.Logger;

final class EventHandlerService {

  private static final Logger log = Logger.getLogger(EventHandlerResource.class.getName());
  private static final DatabaseManager dm = DatabaseManager.getInstance();
  private static final HashMap<String, Object> restrictionMap = new HashMap<>();

  private static synchronized List<EventFilter> getMatchingEventFilters(PublishEvent event) {
    restrictionMap.clear();
    restrictionMap.put("eventType", event.getType());
    List<EventFilter> filters = dm.getAll(EventFilter.class, restrictionMap);
    // Remove the filter if the event source is not in the filter's source list
    filters.removeIf(current -> !current.getSources().contains(event.getSource()));
    // Remove the filter if the event timestamp is not between the filter's startDate and endDate
    filters.removeIf(current -> !(event.getTimestamp().isAfter(current.getStartDate()) && event.getTimestamp().isBefore(current.getEndDate())));
    // Remove the filter if MatchMetadata = true and the event and filter metadata do not match perfectly
    filters.removeIf(current -> current.getMatchMetadata() && !event.getEventMetadata().equals(current.getFilterMetadata()));

    return filters;
  }

  private static synchronized boolean sendRequest(String url, PublishEvent event) {
    event.setDeliveryCompleteUri(null);
    try {
      Utility.sendRequest(url, "POST", event);
    } catch (Exception e) {
      System.out.println("Publishing event to " + url + " failed.");
      e.printStackTrace();
      return false;
    }
    return true;
  }

  static synchronized Map<String, Boolean> getSubscriberUrls(PublishEvent event) {
    List<EventFilter> filters = getMatchingEventFilters(event);
    List<String> urls = new ArrayList<>();
    for (EventFilter filter : filters) {
      //NOTE hogyan kéne egyszerre támogatni isSecure true és false állapotot a consumereknél?
      // Utilitynek meg lehet adni security contextet, amivel a kérést lehet küldeni (eventhandler cert), de egy extra mező kell hozzá a filterben
      String url;
      try {
        url = Utility.getUri(filter.getConsumer().getAddress(), filter.getPort(), filter.getNotifyUri(), false, false);
      } catch (ArrowheadException | NullPointerException e) {
        e.printStackTrace();
        continue;
      }
      urls.add(url);
    }

    Map<String, Boolean> result = new HashMap<>();
    for (String url : urls) {
      CompletableFuture.supplyAsync(() -> sendRequest(url, event)).thenAcceptAsync(successfulPublish -> result.put(url, successfulPublish));
    }

    CompletableFuture.allOf((CompletableFuture[]) urls.stream().map(
        url -> CompletableFuture.supplyAsync(() -> sendRequest(url, event)).thenAcceptAsync(successfulPublish -> result.put(url, successfulPublish)))
                                                      .toArray()).join();

    System.out.println(result);
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
