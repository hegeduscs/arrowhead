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
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.exception.BadPayloadException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonIgnoreProperties({"alwaysMandatoryFields"})
public class IntraCloudAuthEntry extends ArrowheadBase {

  private static final Set<String> alwaysMandatoryFields = new HashSet<>(Arrays.asList("serviceList", "consumer", "providerList"));

  private ArrowheadSystem consumer;
  private List<ArrowheadSystem> providerList = new ArrayList<>();
  private List<ArrowheadService> serviceList = new ArrayList<>();

  public IntraCloudAuthEntry() {
  }

  public IntraCloudAuthEntry(ArrowheadSystem consumer, List<ArrowheadSystem> providerList, List<ArrowheadService> serviceList) {
    this.consumer = consumer;
    this.providerList = providerList;
    this.serviceList = serviceList;
  }

  public ArrowheadSystem getConsumer() {
    return consumer;
  }

  public void setConsumer(ArrowheadSystem consumer) {
    this.consumer = consumer;
  }

  public List<ArrowheadSystem> getProviderList() {
    return providerList;
  }

  public void setProviderList(List<ArrowheadSystem> providerList) {
    this.providerList = providerList;
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

    if (consumer != null) {
      mandatoryFields = consumer.missingFields(false, mandatoryFields);
    }

    for (ArrowheadSystem provider : providerList) {
      Set<String> fields = provider.missingFields(false, null);
      if (!fields.isEmpty()) {
        mandatoryFields.add("Provider is missing mandatory field(s): " + String.join(", ", fields));
      }
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
