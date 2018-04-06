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
import eu.arrowhead.common.database.ArrowheadCloud;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.exception.BadPayloadException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@JsonIgnoreProperties({"alwaysMandatoryFields"})
public class GSDPoll extends ArrowheadBase {

  private static final Set<String> alwaysMandatoryFields = new HashSet<>(Arrays.asList("requestedService", "requesterCloud"));

  private ArrowheadService requestedService;
  private ArrowheadCloud requesterCloud;

  public GSDPoll() {
  }

  public GSDPoll(ArrowheadService requestedService, ArrowheadCloud requesterCloud) {
    this.requestedService = requestedService;
    this.requesterCloud = requesterCloud;
  }

  public ArrowheadService getRequestedService() {
    return requestedService;
  }

  public void setRequestedService(ArrowheadService requestedService) {
    this.requestedService = requestedService;
  }

  public ArrowheadCloud getRequesterCloud() {
    return requesterCloud;
  }

  public void setRequesterCloud(ArrowheadCloud requesterCloud) {
    this.requesterCloud = requesterCloud;
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
    if (requesterCloud != null) {
      mandatoryFields = requesterCloud.missingFields(false, mandatoryFields);
    }
    if (throwException && !mandatoryFields.isEmpty()) {
      throw new BadPayloadException("Missing mandatory fields for " + getClass().getSimpleName() + ": " + String.join(", ", mandatoryFields));
    }
    return mandatoryFields;
  }

}
