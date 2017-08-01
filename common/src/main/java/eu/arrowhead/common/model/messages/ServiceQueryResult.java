package eu.arrowhead.common.model.messages;

import java.util.ArrayList;
import java.util.List;

public class ServiceQueryResult {

  private List<ProvidedService> serviceQueryData = new ArrayList<>();

  public ServiceQueryResult() {
  }

  public ServiceQueryResult(List<ProvidedService> serviceQueryData) {
    this.serviceQueryData = serviceQueryData;
  }

  public List<ProvidedService> getServiceQueryData() {
    return serviceQueryData;
  }

  public void setServiceQueryData(List<ProvidedService> serviceQueryData) {
    this.serviceQueryData = serviceQueryData;
  }

  public boolean isPayloadEmpty() {
    return serviceQueryData == null || serviceQueryData.isEmpty();
  }

}
