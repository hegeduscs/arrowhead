package eu.arrowhead.common.exception;

public class UnavailableServerException extends RuntimeException {

  private static final long serialVersionUID = -2615685391063948589L;

  public UnavailableServerException(String message) {
    super(message);
  }
}
