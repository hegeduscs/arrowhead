package eu.arrowhead.common.messages;

public class InterCloudAuthResponse {

  private boolean isAuthorized;

  public InterCloudAuthResponse() {
  }

  public InterCloudAuthResponse(boolean isAuthorized) {
    this.isAuthorized = isAuthorized;
  }

  public boolean isAuthorized() {
    return isAuthorized;
  }

  public void setAuthorized(boolean isAuthorized) {
    this.isAuthorized = isAuthorized;
  }

}
