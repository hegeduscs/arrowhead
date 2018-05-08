/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.exception;

/**
 * Used throughout the project to signal authentication and authorization related problems. Results in a 401 HTTP code if it happens during normal
 * operation (after core systems servers started listening for requests). <p> If this exception is thrown at server startup, it signals a problem with
 * a certificate file. Runtime example can be when a system tries to access an existing resource which is not allowed by the
 * <i>AccessControlFilter</i> of the core system.
 */
public class AuthException extends ArrowheadException {

  public AuthException(final String msg, final int errorCode, final String origin, final Throwable cause) {
    super(msg, errorCode, origin, cause);
    this.setExceptionType(ExceptionType.AUTH);
  }

  public AuthException(final String msg, final int errorCode, final String origin) {
    super(msg, errorCode, origin);
    this.setExceptionType(ExceptionType.AUTH);
  }

  public AuthException(String msg, int errorCode, Throwable cause) {
    super(msg, errorCode, cause);
    this.setExceptionType(ExceptionType.AUTH);
  }

  public AuthException(String msg, int errorCode) {
    super(msg, errorCode);
    this.setExceptionType(ExceptionType.AUTH);
  }

  public AuthException(String msg, Throwable cause) {
    super(msg, cause);
    this.setExceptionType(ExceptionType.AUTH);
  }

  public AuthException(String msg) {
    super(msg);
    this.setExceptionType(ExceptionType.AUTH);
  }

}
