package eu.arrowhead.common.model.messages;

import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.ServiceMetadata;
import java.util.ArrayList;
import java.util.List;

public class ServiceRegistryEntry {

  private ArrowheadSystem provider;
  private String serviceURI;
  private List<ServiceMetadata> serviceMetadata = new ArrayList<>();
  private String tSIG_key;
  private String version;

  public ServiceRegistryEntry() {
  }

  public ServiceRegistryEntry(ArrowheadSystem provider, String serviceURI, List<ServiceMetadata> serviceMetadata, String tsig_key, String version) {
    this.provider = provider;
    this.serviceURI = serviceURI;
    this.serviceMetadata = serviceMetadata;
    this.tSIG_key = tsig_key;
    this.version = version;
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

  public String gettSIG_key() {
    return tSIG_key;
  }

  public void settSIG_key(String tSIG_key) {
    this.tSIG_key = tSIG_key;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

}
