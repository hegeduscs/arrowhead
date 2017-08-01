package eu.arrowhead.common.model.messages;

import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;
import java.util.ArrayList;
import java.util.Collection;

public class InterCloudAuthEntry {

  private ArrowheadCloud cloud;
  private Collection<ArrowheadService> serviceList = new ArrayList<>();

  public InterCloudAuthEntry() {
  }

  public InterCloudAuthEntry(ArrowheadCloud cloud, Collection<ArrowheadService> serviceList) {
    this.cloud = cloud;
    this.serviceList = serviceList;
  }

  public ArrowheadCloud getCloud() {
    return cloud;
  }

  public void setCloud(ArrowheadCloud cloud) {
    this.cloud = cloud;
  }

  public Collection<ArrowheadService> getServiceList() {
    return serviceList;
  }

  public void setServiceList(Collection<ArrowheadService> serviceList) {
    this.serviceList = serviceList;
  }

  public boolean isPayloadUsable() {
    if (cloud == null || serviceList.isEmpty() || !cloud.isValidForDatabase()) {
      return false;
    }
    for (ArrowheadService service : serviceList) {
      if (!service.isValidSoft()) {
        return false;
      }
    }
    return true;
  }


}
