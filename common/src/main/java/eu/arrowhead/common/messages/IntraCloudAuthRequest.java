/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.common.messages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.exception.BadPayloadException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@JsonIgnoreProperties({"alwaysMandatoryFields"})
public class IntraCloudAuthRequest extends ArrowheadBase {

  private static final Set<String> alwaysMandatoryFields = new HashSet<>(Arrays.asList("service", "consumer", "providers"));

  private ArrowheadSystem consumer;
  private Set<ArrowheadSystem> providers = new HashSet<>();
  private ArrowheadService service;

  public IntraCloudAuthRequest() {
  }

  public IntraCloudAuthRequest(ArrowheadSystem consumer, Set<ArrowheadSystem> providers, ArrowheadService service) {
    this.consumer = consumer;
    this.providers = providers;
    this.service = service;
  }

  public ArrowheadSystem getConsumer() {
    return consumer;
  }

  public void setConsumer(ArrowheadSystem consumer) {
    this.consumer = consumer;
  }

  public Set<ArrowheadSystem> getProviders() {
    return providers;
  }

  public void setProviders(Set<ArrowheadSystem> providers) {
    this.providers = providers;
  }

  public ArrowheadService getService() {
    return service;
  }

  public void setService(ArrowheadService service) {
    this.service = service;
  }

  public Set<String> missingFields(boolean throwException, Set<String> mandatoryFields) {
    if (mandatoryFields == null) {
      mandatoryFields = new HashSet<>(alwaysMandatoryFields);
    }
    mandatoryFields.addAll(alwaysMandatoryFields);
    Set<String> nonNullFields = getFieldNamesWithNonNullValue();
    mandatoryFields.removeAll(nonNullFields);
    if (service != null) {
      mandatoryFields = service.missingFields(false, false, mandatoryFields);
    }
    if (consumer != null) {
      mandatoryFields = consumer.missingFields(false, mandatoryFields);
    }

    for (ArrowheadSystem provider : providers) {
      Set<String> fields = provider.missingFields(false, null);
      if (!fields.isEmpty()) {
        mandatoryFields.add("Provider is missing mandatory field(s): " + String.join(", ", fields));
      }
    }

    if (throwException && !mandatoryFields.isEmpty()) {
      throw new BadPayloadException("Missing mandatory fields for " + getClass().getSimpleName() + ": " + String.join(", ", mandatoryFields));
    }
    return mandatoryFields;
  }

}
