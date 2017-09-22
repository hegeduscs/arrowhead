package eu.arrowhead.common.messages;

import eu.arrowhead.common.database.ArrowheadCloud;
import eu.arrowhead.common.database.ArrowheadService;

public class InterCloudAuthRequest {

  private ArrowheadCloud cloud;
  private ArrowheadService service;

  public InterCloudAuthRequest() {
  }

  public InterCloudAuthRequest(ArrowheadCloud cloud, ArrowheadService service) {
    this.cloud = cloud;
    this.service = service;
  }

  public ArrowheadCloud getCloud() {
    return cloud;
  }

  public void setCloud(ArrowheadCloud cloud) {
    this.cloud = cloud;
  }

  public ArrowheadService getService() {
    return service;
  }

  public void setService(ArrowheadService service) {
    this.service = service;
  }

  public boolean isPayloadUsable() {
    return cloud != null && service != null && cloud.isValidForDatabase() && service.isValidForDatabase();
  }

}
