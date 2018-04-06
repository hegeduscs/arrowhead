/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.common.messages;

import java.util.ArrayList;
import java.util.List;

public class GSDResult {

  private List<GSDAnswer> response = new ArrayList<>();

  public GSDResult() {
  }

  public GSDResult(List<GSDAnswer> response) {
    this.response = response;
  }

  public List<GSDAnswer> getResponse() {
    return response;
  }

  public void setResponse(List<GSDAnswer> response) {
    this.response = response;
  }

  public boolean isValid() {
    return response != null && !response.isEmpty();
  }

}
