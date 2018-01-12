package eu.arrowhead.common.json.supportadapter;

import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.ServiceRegistryEntry;

public class ServiceRegistryEntrySupport {

  private ArrowheadServiceSupport providedService;
  private ArrowheadSystem provider;
  private String serviceURI;

  public ServiceRegistryEntrySupport() {
  }

  public ServiceRegistryEntrySupport(ServiceRegistryEntry entry) {
    ArrowheadService service = entry.getProvidedService();
    ArrowheadServiceSupport legacyService = new ArrowheadServiceSupport(service.getServiceGroup(), service.getServiceDefinition(),
                                                                        service.getInterfaces(), service.getServiceMetadata());
    this.providedService = legacyService;
    this.provider = entry.getProvider();
    this.serviceURI = entry.getServiceURI();
  }

  public ServiceRegistryEntrySupport(ArrowheadServiceSupport providedService, ArrowheadSystem provider, String serviceURI) {
    this.providedService = providedService;
    this.provider = provider;
    this.serviceURI = serviceURI;
  }

  public ArrowheadServiceSupport getProvidedService() {
    return providedService;
  }

  public void setProvidedService(ArrowheadServiceSupport providedService) {
    this.providedService = providedService;
  }

  public ArrowheadSystem getProvider() {
    return provider;
  }

  public void setProvider(ArrowheadSystem provider) {
    this.provider = provider;
  }

  public String getServiceURI() {
    return serviceURI;
  }

  public void setServiceURI(String serviceURI) {
    this.serviceURI = serviceURI;
  }

}
