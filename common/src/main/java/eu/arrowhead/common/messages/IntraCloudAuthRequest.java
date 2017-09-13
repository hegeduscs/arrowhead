package eu.arrowhead.common.messages;

import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import java.util.HashSet;
import java.util.Set;

public class IntraCloudAuthRequest {

  private ArrowheadSystem consumer;
  private Set<ArrowheadSystem> providers = new HashSet<>();
  private ArrowheadService service;

  public IntraCloudAuthRequest() {
  }

  public IntraCloudAuthRequest(ArrowheadSystem consumer, Set<ArrowheadSystem> providers, ArrowheadService service) {
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

  public Set<ArrowheadSystem> getProviders() {
    return providers;
  }

  public void setProviders(Set<ArrowheadSystem> providers) {
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
