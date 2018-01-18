package eu.arrowhead.common.exception;

/**
 * Thrown if a resource receives a HTTP payload which have missing mandatory fields.
 */
public class BadPayloadException extends ArrowheadException {

  public BadPayloadException(String msg, int errorCode, String exceptionType, String origin, Throwable cause) {
    super(msg, errorCode, exceptionType, origin, cause);
  }

  public BadPayloadException(String msg, int errorCode, String exceptionType, String origin) {
    super(msg, errorCode, exceptionType, origin);
  }

  public BadPayloadException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public BadPayloadException(String msg) {
    super(msg);
  }
}
