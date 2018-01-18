package eu.arrowhead.common.exception;

import java.net.HttpURLConnection;

/**
 * Usually thrown by the Core System resources if a crucial database query (for example the query of the Service Registry or the Authorization) comes
 * back empty during the servicing of the request.
 */
public class DataNotFoundException extends ArrowheadException {

  public DataNotFoundException(final String message) {
    super(HttpURLConnection.HTTP_NOT_FOUND, message);
  }

  public DataNotFoundException(String message, Throwable cause) {
    super(HttpURLConnection.HTTP_NOT_FOUND, message, cause);
  }
}
