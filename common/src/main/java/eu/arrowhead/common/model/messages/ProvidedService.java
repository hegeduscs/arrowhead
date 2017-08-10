package eu.arrowhead.common.model.messages;

import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;

public class ProvidedService {

  private ArrowheadSystem provider;
  private ArrowheadService offered;
  private String serviceURI;

  public ProvidedService() {
  }

  public ProvidedService(ArrowheadSystem provider, ArrowheadService offered, String serviceURI) {
    this.provider = provider;
    this.offered = offered;
    this.serviceURI = serviceURI;
  }

  public ArrowheadSystem getProvider() {
    return provider;
  }

  public void setProvider(ArrowheadSystem provider) {
    this.provider = provider;
  }

  public ArrowheadService getOffered() {
    return offered;
  }

  public void setOffered(ArrowheadService offered) {
    this.offered = offered;
  }

  public String getServiceURI() {
    return serviceURI;
  }

  public void setServiceURI(String serviceURI) {
    this.serviceURI = serviceURI;
  }

  public boolean isValid() {
    return provider != null && provider.isValid() && offered != null && offered.isValid() && serviceURI != null;
  }

}
