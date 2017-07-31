package eu.arrowhead.core.orchestrator.store;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.database.OrchestrationStore;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import java.util.HashMap;
import java.util.List;

public final class StoreService {

  private static DatabaseManager dm = DatabaseManager.getInstance();
  private static HashMap<String, Object> restrictionMap = new HashMap<>();

  /**
   * This method returns all the Orchestration Store entries belonging to a consumer.
   */
  public static List<OrchestrationStore> getStoreEntries(ArrowheadSystem consumer) {
    restrictionMap.clear();
    ArrowheadSystem savedConsumer = getConsumerSystem(consumer.getSystemGroup(), consumer.getSystemName());
    if (savedConsumer == null) {
      return null;
    }

    restrictionMap.put("consumer", savedConsumer);
    return dm.getAll(OrchestrationStore.class, restrictionMap);
  }

  /**
   * This method returns a list of Orchestration Store entries specified by the consumer system and the requested service.
   */
  public static List<OrchestrationStore> getStoreEntries(ArrowheadSystem consumer, ArrowheadService service) {
    restrictionMap.clear();
    ArrowheadSystem savedConsumer = getConsumerSystem(consumer.getSystemGroup(), consumer.getSystemName());
    ArrowheadService savedService = getRequestedService(service.getServiceGroup(), service.getServiceDefinition());
    if (savedConsumer == null || savedService == null) {
      return null;
    }

    boolean hasMatchingInterfaces = false;
    for (String givenInterface : service.getInterfaces()) {
      for (String savedInterface : savedService.getInterfaces()) {
        if (givenInterface.equals(savedInterface)) {
          hasMatchingInterfaces = true;
        }
      }
    }
    if (!hasMatchingInterfaces) {
      return null;
    }

    restrictionMap.put("consumer", savedConsumer);
    restrictionMap.put("service", savedService);
    return dm.getAll(OrchestrationStore.class, restrictionMap);
  }

  /**
   * This method returns the active Orchestration Store entries for a consumer.
   */
  public static List<OrchestrationStore> getActiveStoreEntries(ArrowheadSystem consumer) {
    restrictionMap.clear();
    ArrowheadSystem savedConsumer = getConsumerSystem(consumer.getSystemGroup(), consumer.getSystemName());
    if (savedConsumer == null) {
      return null;
    }

    restrictionMap.put("consumer", savedConsumer);
    restrictionMap.put("isActive", true);
    return dm.getAll(OrchestrationStore.class, restrictionMap);
  }

  /**
   * This method returns all the entries of the Orchestration Store.
   */
  public static List<OrchestrationStore> getAllStoreEntries() {
    restrictionMap.clear();
    return dm.getAll(OrchestrationStore.class, restrictionMap);
  }

  /**
   * This method returns an ArrowheadSystem from the database.
   */
  private static ArrowheadSystem getConsumerSystem(String systemGroup, String systemName) {
    HashMap<String, Object> rm = new HashMap<>();
    rm.put("systemGroup", systemGroup);
    rm.put("systemName", systemName);
    return dm.get(ArrowheadSystem.class, rm);
  }

  /**
   * This method returns an ArrowheadService from the database.
   */
  private static ArrowheadService getRequestedService(String serviceGroup, String serviceDefinition) {
    HashMap<String, Object> rm = new HashMap<>();
    rm.put("serviceGroup", serviceGroup);
    rm.put("serviceDefinition", serviceDefinition);
    return dm.get(ArrowheadService.class, rm);
  }

}
