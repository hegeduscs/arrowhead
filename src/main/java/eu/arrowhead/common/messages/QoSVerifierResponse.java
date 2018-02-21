/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.common.messages;

/**
 * Message Exchanged from QoSVerifierAlgorithm to QoSManager.
 *
 * @author Paulo
 */
public class QoSVerifierResponse {

  private boolean response;
  private RejectMotivationTypes RejectMotivaiton;

  public QoSVerifierResponse() {
  }

  public QoSVerifierResponse(boolean response, RejectMotivationTypes rejectMotivaiton) {
    super();
    this.response = response;
    RejectMotivaiton = rejectMotivaiton;
  }

  public RejectMotivationTypes getRejectMotivation() {
    return RejectMotivaiton;
  }

  public void setRejectMotivaiton(RejectMotivationTypes rejectMotivaiton) {
    RejectMotivaiton = rejectMotivaiton;
  }

  public boolean getResponse() {
    return response;
  }

  public void setResponse(boolean response) {
    this.response = response;
  }

  public enum RejectMotivationTypes {
    ALWAYS, TEMPORARY, COMBINATION
  }

}
