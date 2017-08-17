package eu.arrowhead.common.model.messages;

import eu.arrowhead.common.Utility;
import eu.arrowhead.common.model.ArrowheadService;

public class ServiceQueryForm {

  private ArrowheadService service;
  private boolean pingProviders;
  private boolean metadataSearch;
  private String tsig_key;

  public ServiceQueryForm() {
  }

  public ServiceQueryForm(ArrowheadService service, boolean pingProviders, boolean metadataSearch,
                          String tsig_key) {
      this.service = service;
      this.pingProviders = pingProviders;
      this.metadataSearch = metadataSearch;
      this.tsig_key = tsig_key;
  }

  public ServiceQueryForm(ServiceRequestForm srf) {
    this.service = srf.getRequestedService();
    this.pingProviders = srf.getOrchestrationFlags().get("pingProvider");
    this.metadataSearch = srf.getOrchestrationFlags().get("metadataSearch");
    this.tsig_key = Utility.getCoreSystem("serviceregistry").getAuthenticationInfo();
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

  public String getTsig_key() {
    return tsig_key;
  }

  public void setTsig_key(String tsig_key) {
    this.tsig_key = tsig_key;
  }

  public boolean isValid() {
    return service != null && !service.isValid();
  }

}
