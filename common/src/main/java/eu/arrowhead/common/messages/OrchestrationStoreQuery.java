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
public class OrchestrationStoreQuery extends ArrowheadBase {

  private static final Set<String> alwaysMandatoryFields = new HashSet<>(Arrays.asList("requestedService", "requesterSystem"));

  private ArrowheadService requestedService;
  private ArrowheadSystem requesterSystem;

  public OrchestrationStoreQuery() {
  }

  public OrchestrationStoreQuery(ArrowheadService requestedService, ArrowheadSystem requesterSystem) {
    this.requestedService = requestedService;
    this.requesterSystem = requesterSystem;
  }

  public ArrowheadService getRequestedService() {
    return requestedService;
  }

  public void setRequestedService(ArrowheadService requestedService) {
    this.requestedService = requestedService;
  }

  public ArrowheadSystem getRequesterSystem() {
    return requesterSystem;
  }

  public void setRequesterSystem(ArrowheadSystem requesterSystem) {
    this.requesterSystem = requesterSystem;
  }

  public Set<String> missingFields(boolean throwException, Set<String> mandatoryFields) {
    if (mandatoryFields == null) {
      mandatoryFields = new HashSet<>(alwaysMandatoryFields);
    }
    mandatoryFields.addAll(alwaysMandatoryFields);
    Set<String> nonNullFields = getFieldNamesWithNonNullValue();
    mandatoryFields.removeAll(nonNullFields);
    if (requestedService != null) {
      mandatoryFields = requestedService.missingFields(false, false, mandatoryFields);
    }
    if (requesterSystem != null) {
      mandatoryFields = requesterSystem.missingFields(false, mandatoryFields);
    }
    if (throwException && !mandatoryFields.isEmpty()) {
      throw new BadPayloadException("Missing mandatory fields for " + getClass().getSimpleName() + ": " + String.join(", ", mandatoryFields));
    }
    return mandatoryFields;
  }

}
