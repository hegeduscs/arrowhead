package eu.arrowhead.common.exception;

/**
 * Used by the <i>DatabaseManager</i> class when Hibernate <i>ConstraintViolationException</i>s happen. This can happen when trying to save a new
 * object with the same unique constraint fields as an already existing entry, or when trying to delete an entry which has foreign key constraints in
 * other tables.
 */
public class DuplicateEntryException extends RuntimeException {

  private static final long serialVersionUID = 615148647757242985L;

  public DuplicateEntryException(String message) {
    super(message);
  }

}
