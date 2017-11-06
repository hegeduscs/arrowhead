package eu.arrowhead.core.orchestrator;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.OrchestrationStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class StoreService {

  private static final DatabaseManager dm = DatabaseManager.getInstance();
  private static final HashMap<String, Object> restrictionMap = new HashMap<>();

  /**
   * This method returns the active Orchestration Store entries for a consumer.
   */
  public static List<OrchestrationStore> getDefaultStoreEntries(ArrowheadSystem consumer) {
    restrictionMap.clear();
    ArrowheadSystem savedConsumer = getConsumerSystem(consumer.getSystemGroup(), consumer.getSystemName());
    if (savedConsumer == null) {
      return new ArrayList<>();
    }

    restrictionMap.put("consumer", savedConsumer);
    restrictionMap.put("isDefault", true);
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
      return new ArrayList<>();
    }

    if (!savedService.getInterfaces().isEmpty()) {
      if (!hasMatchingInterfaces(savedService, service)) {
        return new ArrayList<>();
      }
    }

    restrictionMap.put("consumer", savedConsumer);
    restrictionMap.put("service", savedService);
    return dm.getAll(OrchestrationStore.class, restrictionMap);
  }

  /**
   * This private method returns an ArrowheadSystem from the database.
   */
  private static ArrowheadSystem getConsumerSystem(String systemGroup, String systemName) {
    HashMap<String, Object> rm = new HashMap<>();
    rm.put("systemGroup", systemGroup);
    rm.put("systemName", systemName);
    return dm.get(ArrowheadSystem.class, rm);
  }

  /**
   * This private method returns an ArrowheadService from the database.
   */
  private static ArrowheadService getRequestedService(String serviceGroup, String serviceDefinition) {
    HashMap<String, Object> rm = new HashMap<>();
    rm.put("serviceGroup", serviceGroup);
    rm.put("serviceDefinition", serviceDefinition);
    return dm.get(ArrowheadService.class, rm);
  }

  private static boolean hasMatchingInterfaces(ArrowheadService savedService, ArrowheadService givenService) {
    for (String givenInterface : givenService.getInterfaces()) {
      for (String savedInterface : savedService.getInterfaces()) {
        if (givenInterface.equals(savedInterface)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * This method returns all the entries of the Orchestration Store.
   */
  public static List<OrchestrationStore> getAllStoreEntries() {
    restrictionMap.clear();
    return dm.getAll(OrchestrationStore.class, restrictionMap);
  }

}
