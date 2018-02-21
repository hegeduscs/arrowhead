/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.common.messages;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.arrowhead.common.database.ArrowheadCloud;
import eu.arrowhead.common.database.ArrowheadService;
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

  @JsonIgnore
  public boolean isPayloadUsable() {
    if (cloud == null || serviceList.isEmpty() || !cloud.isValidForDatabase()) {
      return false;
    }
    for (ArrowheadService service : serviceList) {
      if (!service.isValidForDatabase()) {
        return false;
      }
    }
    return true;
  }


}
