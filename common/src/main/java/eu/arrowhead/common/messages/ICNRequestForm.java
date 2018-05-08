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
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.exception.BadPayloadException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@JsonIgnoreProperties({"alwaysMandatoryFields"})
public class ICNRequestForm extends ArrowheadBase {

  private static final Set<String> alwaysMandatoryFields = new HashSet<>(Arrays.asList("requestedService", "targetCloud", "requesterSystem"));

  private ArrowheadService requestedService;
  private ArrowheadCloud targetCloud;
  private ArrowheadSystem requesterSystem;
  private List<ArrowheadSystem> preferredSystems = new ArrayList<>();
  private Map<String, Boolean> negotiationFlags = new HashMap<>();

  public ICNRequestForm() {
  }

  public ICNRequestForm(ArrowheadService requestedService, ArrowheadCloud targetCloud, ArrowheadSystem requesterSystem,
                        List<ArrowheadSystem> preferredSystems, Map<String, Boolean> negotiationFlags) {
    this.requestedService = requestedService;
    this.targetCloud = targetCloud;
    this.requesterSystem = requesterSystem;
    this.preferredSystems = preferredSystems;
    this.negotiationFlags = negotiationFlags;
  }

  public ArrowheadService getRequestedService() {
    return requestedService;
  }

  public void setRequestedService(ArrowheadService requestedService) {
    this.requestedService = requestedService;
  }

  public ArrowheadCloud getTargetCloud() {
    return targetCloud;
  }

  public void setTargetCloud(ArrowheadCloud targetCloud) {
    this.targetCloud = targetCloud;
  }

  public ArrowheadSystem getRequesterSystem() {
    return requesterSystem;
  }

  public void setRequesterSystem(ArrowheadSystem requesterSystem) {
    this.requesterSystem = requesterSystem;
  }

  public List<ArrowheadSystem> getPreferredSystems() {
    return preferredSystems;
  }

  public void setPreferredSystems(List<ArrowheadSystem> preferredSystems) {
    this.preferredSystems = preferredSystems;
  }

  public Map<String, Boolean> getNegotiationFlags() {
    return negotiationFlags;
  }

  public void setNegotiationFlags(Map<String, Boolean> negotiationFlags) {
    this.negotiationFlags = negotiationFlags;
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
    if (targetCloud != null) {
      mandatoryFields = targetCloud.missingFields(false, mandatoryFields);
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
