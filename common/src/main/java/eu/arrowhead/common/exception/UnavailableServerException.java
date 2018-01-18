package eu.arrowhead.common.exception;

import java.net.HttpURLConnection;

/**
 * Thrown when a HTTP request times out because the endpoint is not available.
 */
public class UnavailableServerException extends ArrowheadException {

  public UnavailableServerException(final String message) {
    super(HttpURLConnection.HTTP_UNAVAILABLE, message);
  }

  public UnavailableServerException(String message, Throwable cause) {
    super(HttpURLConnection.HTTP_UNAVAILABLE, message, cause);
  }

}
