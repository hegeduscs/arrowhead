package eu.arrowhead.common.model.messages;

import java.util.ArrayList;
import java.util.List;

class QueryResult {

  private List<ServiceRegistryEntry> providers = new ArrayList<>();

  public QueryResult() {
  }

  public QueryResult(List<ServiceRegistryEntry> providers) {
    this.providers = providers;
  }

  public List<ServiceRegistryEntry> getProviders() {
    return providers;
  }

  public void setProviders(List<ServiceRegistryEntry> providers) {
    this.providers = providers;
  }

  public void addProvider(ServiceRegistryEntry provider) {
    this.providers.add(provider);
  }

}
