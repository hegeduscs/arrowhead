/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.messages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import eu.arrowhead.common.database.ArrowheadCloud;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.exception.BadPayloadException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonIgnoreProperties({"alwaysMandatoryFields"})
public class InterCloudAuthEntry extends ArrowheadBase {

  private static final Set<String> alwaysMandatoryFields = new HashSet<>(Arrays.asList("serviceList", "cloud"));

  private ArrowheadCloud cloud;
  private List<ArrowheadService> serviceList = new ArrayList<>();

  public InterCloudAuthEntry() {
  }

  public InterCloudAuthEntry(ArrowheadCloud cloud, List<ArrowheadService> serviceList) {
    this.cloud = cloud;
    this.serviceList = serviceList;
  }

  public ArrowheadCloud getCloud() {
    return cloud;
  }

  public void setCloud(ArrowheadCloud cloud) {
    this.cloud = cloud;
  }

  public List<ArrowheadService> getServiceList() {
    return serviceList;
  }

  public void setServiceList(List<ArrowheadService> serviceList) {
    this.serviceList = serviceList;
  }

  public Set<String> missingFields(boolean throwException, Set<String> mandatoryFields) {
    if (mandatoryFields == null) {
      mandatoryFields = new HashSet<>(alwaysMandatoryFields);
    }
    mandatoryFields.addAll(alwaysMandatoryFields);
    Set<String> nonNullFields = getFieldNamesWithNonNullValue();
    mandatoryFields.removeAll(nonNullFields);

    if (cloud != null) {
      mandatoryFields = cloud.missingFields(false, mandatoryFields);
    }

    for (ArrowheadService service : serviceList) {
      Set<String> fields = service.missingFields(false, false, null);
      if (!fields.isEmpty()) {
        mandatoryFields.add("Service is missing mandatory field(s): " + String.join(", ", fields));
      }
    }

    if (throwException && !mandatoryFields.isEmpty()) {
      throw new BadPayloadException("Missing mandatory fields for " + getClass().getSimpleName() + ": " + String.join(", ", mandatoryFields));
    }
    return mandatoryFields;
  }

}
