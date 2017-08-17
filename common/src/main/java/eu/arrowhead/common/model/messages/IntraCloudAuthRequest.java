package eu.arrowhead.common.model.messages;

import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import java.util.ArrayList;
import java.util.List;

public class IntraCloudAuthRequest {

  private ArrowheadSystem consumer;
  private List<ArrowheadSystem> providers = new ArrayList<>();
  private ArrowheadService service;

  public IntraCloudAuthRequest() {
  }

  public IntraCloudAuthRequest(ArrowheadSystem consumer, List<ArrowheadSystem> providers, ArrowheadService service, boolean generateToken) {
    this.consumer = consumer;
    this.providers = providers;
    this.service = service;
  }

  public ArrowheadSystem getConsumer() {
    return consumer;
  }

  public void setConsumer(ArrowheadSystem consumer) {
    this.consumer = consumer;
  }

  public List<ArrowheadSystem> getProviders() {
    return providers;
  }

  public void setProviders(List<ArrowheadSystem> providers) {
    this.providers = providers;
  }

  public ArrowheadService getService() {
    return service;
  }

  public void setService(ArrowheadService service) {
    this.service = service;
  }

  public boolean isValid() {
    if (consumer == null || service == null || providers.isEmpty() || !consumer.isValidForDatabase() || !service.isValidForDatabase()) {
      return false;
    }
    for (ArrowheadSystem provider : providers) {
      if (!provider.isValidForDatabase()) {
        return false;
      }
    }
    return true;
  }

}
