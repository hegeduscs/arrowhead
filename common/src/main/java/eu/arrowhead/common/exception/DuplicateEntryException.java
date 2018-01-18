package eu.arrowhead.common.exception;

import java.net.HttpURLConnection;

/**
 * Used by the <i>DatabaseManager</i> class when Hibernate <i>ConstraintViolationException</i>s happen. This can happen when trying to save a new
 * object with the same unique constraint fields as an already existing entry, or when trying to delete an entry which has foreign key constraints in
 * other tables.
 */
public class DuplicateEntryException extends ArrowheadException {

  public DuplicateEntryException(final String message) {
    super(HttpURLConnection.HTTP_BAD_REQUEST, message);
  }

  public DuplicateEntryException(String message, Throwable cause) {
    super(HttpURLConnection.HTTP_BAD_REQUEST, message, cause);
  }

}
