package eu.arrowhead.common.json.support;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.arrowhead.common.database.ServiceRegistryEntry;

public class ServiceRegistryEntrySupport {

  private ArrowheadServiceSupport providedService;
  private ArrowheadSystemSupport provider;
  private String serviceURI;

  private int version = 1;
  @JsonProperty("UDP")
  private boolean UDP = false;

  public ServiceRegistryEntrySupport() {
  }

  public ServiceRegistryEntrySupport(ServiceRegistryEntry entry) {
    this.providedService = new ArrowheadServiceSupport(entry.getProvidedService());
    this.provider = new ArrowheadSystemSupport(entry.getProvider());
    this.serviceURI = entry.getServiceURI();
  }

  public ServiceRegistryEntrySupport(ArrowheadServiceSupport providedService, ArrowheadSystemSupport provider, String serviceURI) {
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

  public ArrowheadSystemSupport getProvider() {
    return provider;
  }

  public void setProvider(ArrowheadSystemSupport provider) {
    this.provider = provider;
  }

  public String getServiceURI() {
    return serviceURI;
  }

  public void setServiceURI(String serviceURI) {
    this.serviceURI = serviceURI;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public boolean isUDP() {
    return UDP;
  }

  public void setUDP(boolean UDP) {
    this.UDP = UDP;
  }
}
