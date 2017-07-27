package eu.arrowhead.common.model.messages;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class QueryResult {

  private List<ProvidedService> providers = new ArrayList<ProvidedService>();

  public QueryResult() {
    super();
  }

  public QueryResult(List<ProvidedService> providers) {
    super();
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
