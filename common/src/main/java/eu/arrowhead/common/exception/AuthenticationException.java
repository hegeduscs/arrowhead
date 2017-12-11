package eu.arrowhead.common.exception;

/**
 * Used throughout the project to signal authentication and authorization related problems. Results in a 401 HTTP code if it happens during normal
 * operation (after core systems servers started listening for requests).
 * <p>
 * If this exception is thrown at server startup, it signals a problem with a certificate file. Runtime example can be when a system tries to access
 * an existing resource which is not allowed by the <i>AccessControlFilter</i> of the core system.
 */
public class AuthenticationException extends RuntimeException {

  private static final long serialVersionUID = -6404483587525575152L;

  public AuthenticationException(String message) {
    super(message);
  }

  public AuthenticationException(String message, Throwable e) {
    super(message, e);
  }
}
