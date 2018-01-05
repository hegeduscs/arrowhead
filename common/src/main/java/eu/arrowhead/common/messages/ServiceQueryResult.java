package eu.arrowhead.common.messages;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.arrowhead.common.database.ServiceRegistryEntry;
import java.util.ArrayList;
import java.util.List;

public class ServiceQueryResult {

  private List<ServiceRegistryEntry> serviceQueryData = new ArrayList<>();

  public ServiceQueryResult() {
  }

  public ServiceQueryResult(List<ServiceRegistryEntry> serviceQueryData) {
    this.serviceQueryData = serviceQueryData;
  }

  public List<ServiceRegistryEntry> getServiceQueryData() {
    return serviceQueryData;
  }

  public void setServiceQueryData(List<ServiceRegistryEntry> serviceQueryData) {
    this.serviceQueryData = serviceQueryData;
  }

  @JsonIgnore
  public boolean isValid() {
    return serviceQueryData != null && !serviceQueryData.isEmpty();
  }

}
