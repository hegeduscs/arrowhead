package eu.arrowhead.common.exception;

public class DuplicateEntryException extends RuntimeException {

  private static final long serialVersionUID = 615148647757242985L;

  public DuplicateEntryException(String message) {
    super(message);
  }

}
