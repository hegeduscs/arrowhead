package eu.arrowhead.common.exception;

import java.net.HttpURLConnection;

/**
 * Thrown if a resource receives a HTTP payload which have missing mandatory fields. The exception message mentions what are the mandatory fields each
 * case.
 */
public class BadPayloadException extends ArrowheadException {

  public BadPayloadException(final String message) {
    super(HttpURLConnection.HTTP_BAD_REQUEST, message);
  }

  public BadPayloadException(String message, Throwable cause) {
    super(HttpURLConnection.HTTP_BAD_REQUEST, message, cause);
  }
}
