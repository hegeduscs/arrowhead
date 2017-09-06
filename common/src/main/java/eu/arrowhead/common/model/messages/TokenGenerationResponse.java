package eu.arrowhead.common.model.messages;

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