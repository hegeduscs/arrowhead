package eu.arrowhead.common.messages;

import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import java.util.List;

public class ServiceRegistryEntry {

  //mandatory fields
  private ArrowheadService providedService;
  private ArrowheadSystem provider;

  //non-mandatory fields
  private String serviceURI;
  private int version = 1;
  private boolean isUDP = false;

  //only for backwards compatibility, non-mandatory fields
  private List<ServiceMetadata> serviceMetadata;
  private List<String> interfaces;
  private String TSIG_key;

  public ServiceRegistryEntry() {
  }

  public ServiceRegistryEntry(ArrowheadService providedService, ArrowheadSystem provider, String serviceURI) {
    this.providedService = providedService;
    this.provider = provider;
    this.serviceURI = serviceURI;
  }

  public ArrowheadService getProvidedService() {
    return providedService;
  }

  public void setProvidedService(ArrowheadService providedService) {
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

  public List<ServiceMetadata> getServiceMetadata() {
    return serviceMetadata;
  }

  public void setServiceMetadata(List<ServiceMetadata> serviceMetadata) {
    this.serviceMetadata = serviceMetadata;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public List<String> getInterfaces() {
    return interfaces;
  }

  public void setInterfaces(List<String> interfaces) {
    this.interfaces = interfaces;
  }

  public boolean isUDP() {
    return isUDP;
  }

  public void setUDP(boolean UDP) {
    isUDP = UDP;
  }

  public String getTSIG_key() {
    return TSIG_key;
  }

  public void setTSIG_key(String TSIG_key) {
    this.TSIG_key = TSIG_key;
  }

  public boolean isValid() {
    return provider != null && provider.isValid();
  }

  public boolean isValidFully () {
    return provider != null && provider.isValid() && providedService != null && providedService.isValid();
  }

}
