package eu.arrowhead.common.exception;

/**
 * Thrown when a HTTP request times out because the endpoint is not available.
 */
public class UnavailableServerException extends RuntimeException {

  private static final long serialVersionUID = -2615685391063948589L;

  public UnavailableServerException(String message) {
    super(message);
  }
}
