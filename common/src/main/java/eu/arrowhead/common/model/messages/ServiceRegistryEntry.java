package eu.arrowhead.common.model.messages;

import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.ServiceMetadata;
import java.util.ArrayList;
import java.util.List;

public class ServiceRegistryEntry {
  private ArrowheadService providedService;
  private ArrowheadSystem provider;
  private String serviceURI;
  private String TSIG_key;
  private String version;

  //backwards compat
  private List<ServiceMetadata> serviceMetadata = new ArrayList<>();


  //backwards compatibility towards G3.2 M1
  private List<String> interfaces;

  public ServiceRegistryEntry() {
  }

  public ServiceRegistryEntry(ArrowheadSystem provider, String serviceURI, List<ServiceMetadata> serviceMetadata,
                              String tsig_key, String version, List<String> interfaces) {
    this.interfaces = interfaces;
    this.provider = provider;
    this.serviceURI = serviceURI;
    this.serviceMetadata = serviceMetadata;
    this.TSIG_key = tsig_key;
    if (version != null) this.version = version; else this.version = "1.0";
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

  public String getTSIG_key() {
    return TSIG_key;
  }

  public void setTSIG_key(String TSIG_key) {
    this.TSIG_key = TSIG_key;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public List<String> getInterfaces() {
    return interfaces;
  }

  public void setInterfaces(List<String> interfaces) {
    this.interfaces = interfaces;
  }

  public ArrowheadService getProvidedService() {
    return providedService;
  }

  public void setProvidedService(ArrowheadService providedService) {
    this.providedService = providedService;
  }

  public boolean isValid() {
    return provider != null && provider.isValid() && serviceURI != null && TSIG_key != null;
  }

}
