package eu.arrowhead.common.exception;

/**
 * Usually thrown by the Core System resources if a crucial database query (for example the query of the Service Registry or the Authorization) comes
 * back empty during the servicing of the request.
 */
public class DataNotFoundException extends RuntimeException {

  private static final long serialVersionUID = -1622261264080480479L;

  public DataNotFoundException(String message) {
    super(message);
  }
}
