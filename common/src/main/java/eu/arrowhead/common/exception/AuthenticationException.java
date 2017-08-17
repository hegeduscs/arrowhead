package eu.arrowhead.common.exception;

//TODO put javadoc comments to exceptions, explaining when they are used
public class AuthenticationException extends RuntimeException {

  private static final long serialVersionUID = -6404483587525575152L;

  public AuthenticationException(String message) {
    super(message);
  }
}
