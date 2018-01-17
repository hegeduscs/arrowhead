package eu.arrowhead.common.exception;

public class ArrowheadException extends RuntimeException {

  private final int errorCode;

  /**
   * Constructor.
   *
   * @param errorCode the HTTP status code for this exception
   * @param msg human readable message
   * @param cause reason for this exception
   */
  public ArrowheadException(final int errorCode, final String msg, final Throwable cause) {
    super(msg, cause);
    this.errorCode = errorCode;
  }

  /**
   * Constructor.
   *
   * @param errorCode the HTTP status code for this exception
   * @param msg human readable message
   */
  public ArrowheadException(final int errorCode, final String msg) {
    super(msg);
    this.errorCode = errorCode;
  }

  /**
   * Return the HTTP status code for this exception.
   *
   * @return the HTTP status code
   */
  public int getErrorCode() {
    return errorCode;
  }

}
