package eu.arrowhead.common.model.messages;

import eu.arrowhead.common.model.ArrowheadService;

public class ServiceQueryForm {

  private ArrowheadService service;
  private boolean pingProviders;
  private boolean metadataSearch;
  //TODO?
  private int version = 1;

  public ServiceQueryForm() {
  }

  public ServiceQueryForm(ArrowheadService service, boolean pingProviders, boolean metadataSearch) {
      this.service = service;
      this.pingProviders = pingProviders;
      this.metadataSearch = metadataSearch;
  }

  public ArrowheadService getService() {
      return service;
  }

  public void setService(ArrowheadService service) {
      this.service = service;
  }

  public boolean isPingProviders() {
    return pingProviders;
  }

  public void setPingProviders(boolean pingProviders) {
    this.pingProviders = pingProviders;
  }

  public boolean isMetadataSearch() {
    return metadataSearch;
  }

  public void setMetadataSearch(boolean metadataSearch) {
    this.metadataSearch = metadataSearch;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public boolean isValid() {
    return service != null && !service.isValid();
  }

}
