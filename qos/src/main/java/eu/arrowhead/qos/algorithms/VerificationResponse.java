/*
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.qos.algorithms;

/**
 * @author Paulo
 */
public class VerificationResponse {

  private Boolean isPossible;
  private String reason;

  public VerificationResponse() {
  }

  public VerificationResponse(Boolean isPossible, String reason) {
    this.isPossible = isPossible;
    this.reason = reason;
  }

  public Boolean getIsPossible() {
    return isPossible;
  }

  public void setIsPossible(Boolean isPossible) {
    this.isPossible = isPossible;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

}
