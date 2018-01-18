package eu.arrowhead.common.exception;

public class ArrowheadException extends RuntimeException {

  private int errorCode;
  private final String exceptionType;
  private final String origin;

  public ArrowheadException(final String msg, final int errorCode, final String exceptionType, final String origin, final Throwable cause) {
    super(msg, cause);
    this.errorCode = errorCode;
    this.exceptionType = exceptionType;
    this.origin = origin;
  }

  public ArrowheadException(final String msg, final int errorCode, final String exceptionType, final String origin) {
    super(msg);
    this.errorCode = errorCode;
    this.exceptionType = exceptionType;
    this.origin = origin;
  }

  public ArrowheadException(final String msg, final Throwable cause) {
    super(msg, cause);
    this.errorCode = 0;
    this.exceptionType = null;
    this.origin = null;
  }

  public ArrowheadException(final String msg) {
    super(msg);
    this.errorCode = 0;
    this.exceptionType = null;
    this.origin = null;
  }

  public int getErrorCode() {
    return errorCode;
  }

  public String getExceptionType() {
    return exceptionType;
  }

  public String getOrigin() {
    return origin;
  }
}
