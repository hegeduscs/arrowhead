package eu.arrowhead.common.messages;

public class InterCloudAuthResponse {

  private boolean authorized;

  public InterCloudAuthResponse() {
  }

  public InterCloudAuthResponse(boolean isAuthorized) {
    this.authorized = isAuthorized;
  }

  public boolean isAuthorized() {
    return authorized;
  }

  public void setAuthorized(boolean isAuthorized) {
    this.authorized = isAuthorized;
  }

}
