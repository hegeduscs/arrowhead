package eu.arrowhead.common.exception;

/**
 * Thrown if a resource receives a HTTP payload which have missing mandatory fields. The exception message mentions what are the mandatory fields each
 * case.
 */
public class BadPayloadException extends RuntimeException {

  private static final long serialVersionUID = -1835158132522682880L;

  public BadPayloadException(String message) {
    super(message);
  }
}
