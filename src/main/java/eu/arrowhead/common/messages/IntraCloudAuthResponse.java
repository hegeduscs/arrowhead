/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.messages;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.misc.ArrowheadSystemKeyDeserializer;
import java.util.HashMap;

public class IntraCloudAuthResponse {

  @JsonDeserialize(keyUsing = ArrowheadSystemKeyDeserializer.class)
  private HashMap<ArrowheadSystem, Boolean> authorizationState = new HashMap<>();

  public IntraCloudAuthResponse() {
  }

  public IntraCloudAuthResponse(HashMap<ArrowheadSystem, Boolean> authorizationState) {
    this.authorizationState = authorizationState;
  }

  public HashMap<ArrowheadSystem, Boolean> getAuthorizationMap() {
    return authorizationState;
  }

  public void setAuthorizationMap(HashMap<ArrowheadSystem, Boolean> authorizationState) {
    this.authorizationState = authorizationState;
  }

  public boolean isValid() {
    return !authorizationState.isEmpty();
  }

}
