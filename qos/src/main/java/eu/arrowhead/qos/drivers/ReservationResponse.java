/*
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

import java.util.HashMap;
import java.util.Map;

ackage eu.arrowhead.qos.drivers;

/**
 * @author Paulo
 */
public class ReservationResponse {

  private boolean success;
  private String reason;
  private Map<String, String> networkConfiguration = new HashMap<>();

  public ReservationResponse() {
  }

  public ReservationResponse(boolean success, String reason, Map<String, String> networkConfiguration) {
    this.success = success;
    this.reason = reason;
    this.networkConfiguration = networkConfiguration;
  }

  public boolean getSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public Map<String, String> getNetworkConfiguration() {
    return networkConfiguration;
  }

  public void setNetworkConfiguration(Map<String, String> networkConfiguration) {
    this.networkConfiguration = networkConfiguration;
  }

}
