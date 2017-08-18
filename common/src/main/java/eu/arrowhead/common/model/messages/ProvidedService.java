package eu.arrowhead.common.model.messages;

import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;

public class ProvidedService {

  private ArrowheadSystem provider;
  private ArrowheadService offered;
  private String serviceURI;
  private String version = "1.0";
  private boolean isUDP = false;

  public ProvidedService() {
  }

  public ProvidedService(ArrowheadSystem provider, ArrowheadService offered,
                         String serviceURI, String serviceInterface, String version) {
    this.provider = provider;
    this.offered = offered;
    this.serviceURI = serviceURI;
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

  public ArrowheadService getOffered() {
    return offered;
  }

  public void setOffered(ArrowheadService offered) {
    this.offered = offered;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public boolean isUDP() {
        return isUDP;
  }

    public void setUDP(boolean UDP) {
        isUDP = UDP;
    }

  public boolean isValid() {
    return provider != null && provider.isValid() && serviceURI != null;
  }
}
