/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.messages;

import java.util.ArrayList;
import java.util.List;

public class TokenGenerationResponse {

  private List<TokenData> tokenData = new ArrayList<>();

  public TokenGenerationResponse() {
  }

  public TokenGenerationResponse(List<TokenData> tokenData) {
    this.tokenData = tokenData;
  }

  public List<TokenData> getTokenData() {
    return tokenData;
  }

  public void setTokenData(List<TokenData> tokenData) {
    this.tokenData = tokenData;
  }

}