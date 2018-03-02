package eu.arrowhead.core.serviceregistry;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.ServiceRegistryEntry;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.messages.ServiceQueryForm;
import eu.arrowhead.common.messages.ServiceQueryResult;
import eu.arrowhead.core.ArrowheadMain;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;

public final class ServiceRegistryService {

  static final DatabaseManager dm = DatabaseManager.getInstance();
  static final int timeout = Integer.valueOf(ArrowheadMain.getProp().getProperty("ping_timeout", "10000"));

  private static final HashMap<String, Object> restrictionMap = new HashMap<>();
  private static final Logger log = Logger.getLogger(ServiceRegistryService.class.getName());

  private ServiceRegistryService() throws AssertionError {
    throw new AssertionError("ServiceRegistryService is a non-instantiable class");
  }

  public static ServiceQueryResult queryRegistry(ServiceQueryForm queryForm) {
    if (!queryForm.isValid()) {
      log.error("queryRegistry throws BadPayloadException");
      throw new BadPayloadException("ServiceQueryForm has missing/incomplete mandatory field(s).", 400, "ServiceRegistryService:queryRegistry");
    }

    restrictionMap.clear();
    restrictionMap.put("serviceDefinition", queryForm.getService().getServiceDefinition());
    ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);
    if (service == null) {
      log.info("Service " + queryForm.getService().toString() + " is not in the registry.");
      return new ServiceQueryResult();
    }

    restrictionMap.clear();
    restrictionMap.put("providedService", service);
    List<ServiceRegistryEntry> providedServices = dm.getAll(ServiceRegistryEntry.class, restrictionMap);
    for (ServiceRegistryEntry entry : providedServices) {
      entry.fromDatabase();
    }

    //NOTE add version filter too later, if deemed needed
    if (queryForm.isMetadataSearch()) {
      RegistryUtils.filterOnMeta(providedServices, queryForm.getService().getServiceMetadata());
    }
    if (queryForm.isPingProviders()) {
      RegistryUtils.filterOnPing(providedServices);
    }

    log.info("Service " + queryForm.getService().toString() + " queried successfully.");
    return new ServiceQueryResult(providedServices);
  }

  public static ServiceRegistryEntry registerService(ServiceRegistryEntry entry) {
    log.debug("SR reg service: " + entry.getProvidedService() + " provider: " + entry.getProvider() + " serviceURI: " + entry.getServiceURI());

    entry.toDatabase();

    restrictionMap.clear();
    restrictionMap.put("serviceDefinition", entry.getProvidedService().getServiceDefinition());
    ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);
    if (service == null) {
      service = dm.save(entry.getProvidedService());
    } else {
      service.setInterfaces(entry.getProvidedService().getInterfaces());
      dm.merge(service);
    }
    entry.setProvidedService(service);

    restrictionMap.clear();
    restrictionMap.put("systemName", entry.getProvider().getSystemName());
    ArrowheadSystem provider = dm.get(ArrowheadSystem.class, restrictionMap);
    if (provider == null) {
      provider = dm.save(entry.getProvider());
    } else {
      provider.setAddress(entry.getProvider().getAddress());
      provider.setAuthenticationInfo(entry.getProvider().getAuthenticationInfo());
      dm.merge(provider);
    }
    entry.setProvider(provider);

    ServiceRegistryEntry savedEntry = dm.save(entry);
    savedEntry.fromDatabase();
    log.info("New ServiceRegistryEntry " + entry.toString() + " is saved.");
    return savedEntry;
  }

  public static ServiceRegistryEntry removeService(ServiceRegistryEntry entry) {
    log.debug("SR remove service: " + entry.getProvidedService() + " provider: " + entry.getProvider() + " serviceURI: " + entry.getServiceURI());

    restrictionMap.clear();
    restrictionMap.put("serviceDefinition", entry.getProvidedService().getServiceDefinition());
    ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);

    restrictionMap.clear();
    restrictionMap.put("systemName", entry.getProvider().getSystemName());
    ArrowheadSystem provider = dm.get(ArrowheadSystem.class, restrictionMap);

    restrictionMap.clear();
    restrictionMap.put("providedService", service);
    restrictionMap.put("provider", provider);
    ServiceRegistryEntry retrievedEntry = dm.get(ServiceRegistryEntry.class, restrictionMap);
    if (retrievedEntry != null) {
      dm.delete(retrievedEntry);
      retrievedEntry.fromDatabase();
      log.info("ServiceRegistryEntry " + retrievedEntry.toString() + " deleted.");
      return retrievedEntry;
    } else {
      log.info("ServiceRegistryEntry " + entry.toString() + " was not found in the SR to delete.");
      return null;
    }
  }

}
