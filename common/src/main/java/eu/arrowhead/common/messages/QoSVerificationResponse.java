/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.messages;

import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.messages.QoSVerifierResponse.RejectMotivationTypes;
import java.util.HashMap;
import java.util.Map;

public class QoSVerificationResponse {

  private Map<ArrowheadSystem, Boolean> response = new HashMap<>();
  private Map<ArrowheadSystem, QoSVerifierResponse.RejectMotivationTypes> rejectMotivation = new HashMap<>();

  public QoSVerificationResponse() {
  }

  public QoSVerificationResponse(Map<ArrowheadSystem, Boolean> response, Map<ArrowheadSystem, RejectMotivationTypes> rejectMotivation) {
    this.response = response;
    this.rejectMotivation = rejectMotivation;
  }

  public Map<ArrowheadSystem, Boolean> getResponse() {
    return response;
  }

  public void setResponse(Map<ArrowheadSystem, Boolean> response) {
    this.response = response;
  }

  public Map<ArrowheadSystem, QoSVerifierResponse.RejectMotivationTypes> getRejectMotivation() {
    return rejectMotivation;
  }

  public void setRejectMotivation(Map<ArrowheadSystem, QoSVerifierResponse.RejectMotivationTypes> rejectMotivation) {
    this.rejectMotivation = rejectMotivation;
  }

  public void addResponse(ArrowheadSystem system, Boolean resp) {
    response.put(system, resp);
  }

  public void addRejectMotivation(ArrowheadSystem system, QoSVerifierResponse.RejectMotivationTypes rejectType) {
    rejectMotivation.put(system, rejectType);
  }

}
