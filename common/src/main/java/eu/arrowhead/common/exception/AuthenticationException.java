package eu.arrowhead.common.exception;

/**
 * Used throughout the project to signal authentication and authorization related problems. Results in a 401 HTTP code if it happens during normal
 * operation (after core systems servers started listening for requests).
 * <p>
 * If this exception is thrown at server startup, it signals a problem with a certificate file. Runtime example can be when a system tries to access
 * an existing resource which is not allowed by the <i>AccessControlFilter</i> of the core system.
 */
public class AuthenticationException extends ArrowheadException {

  public AuthenticationException(String msg, int errorCode, String exceptionType, String origin, Throwable cause) {
    super(msg, errorCode, exceptionType, origin, cause);
  }

  public AuthenticationException(String msg, int errorCode, String exceptionType, String origin) {
    super(msg, errorCode, exceptionType, origin);
  }

  public AuthenticationException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public AuthenticationException(String msg) {
    super(msg);
  }
}
