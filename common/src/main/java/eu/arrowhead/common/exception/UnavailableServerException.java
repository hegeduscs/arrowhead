package eu.arrowhead.common.exception;

/**
 * Thrown when a HTTP request times out because the endpoint is not available.
 */
public class UnavailableServerException extends ArrowheadException {

  public UnavailableServerException(String msg, int errorCode, String exceptionType, String origin, Throwable cause) {
    super(msg, errorCode, exceptionType, origin, cause);
  }

  public UnavailableServerException(String msg, int errorCode, String exceptionType, String origin) {
    super(msg, errorCode, exceptionType, origin);
  }

  public UnavailableServerException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public UnavailableServerException(String msg) {
    super(msg);
  }
}
