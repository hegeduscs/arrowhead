package eu.arrowhead.common.model.messages;

import java.util.ArrayList;
import java.util.List;

class QueryResult {

  private List<ProvidedService> providers = new ArrayList<>();

  public QueryResult() {
  }

  public QueryResult(List<ProvidedService> providers) {
    this.providers = providers;
  }

  public List<ProvidedService> getProviders() {
    return providers;
  }

  public void setProviders(List<ProvidedService> providers) {
    this.providers = providers;
  }

  public void addProvider(ProvidedService provider) {
    this.providers.add(provider);
  }

}
